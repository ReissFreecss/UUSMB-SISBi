package jsf;

import jpa.entities.LibraryRunLink;
import jsf.util.JsfUtil;
import jsf.util.PaginationHelper;
import jpa.session.LibraryRunLinkFacade;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("libraryRunLinkController")
@SessionScoped
public class LibraryRunLinkController implements Serializable {

    private LibraryRunLink current;
    private DataModel items = null;
    @EJB
    private jpa.session.LibraryRunLinkFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public LibraryRunLinkController() {
    }

    public LibraryRunLink getSelected() {
        if (current == null) {
            current = new LibraryRunLink();
            current.setLibraryRunLinkPK(new jpa.entities.LibraryRunLinkPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private LibraryRunLinkFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (LibraryRunLink) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new LibraryRunLink();
        current.setLibraryRunLinkPK(new jpa.entities.LibraryRunLinkPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getLibraryRunLinkPK().setIdRun(current.getRun().getIdRun());
            current.getLibraryRunLinkPK().setIdLibrary(current.getLibrary().getIdLibrary());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LibraryRunLinkCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (LibraryRunLink) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getLibraryRunLinkPK().setIdRun(current.getRun().getIdRun());
            current.getLibraryRunLinkPK().setIdLibrary(current.getLibrary().getIdLibrary());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LibraryRunLinkUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (LibraryRunLink) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LibraryRunLinkDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public LibraryRunLink getLibraryRunLink(jpa.entities.LibraryRunLinkPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = LibraryRunLink.class)
    public static class LibraryRunLinkControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            LibraryRunLinkController controller = (LibraryRunLinkController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "libraryRunLinkController");
            return controller.getLibraryRunLink(getKey(value));
        }

        jpa.entities.LibraryRunLinkPK getKey(String value) {
            jpa.entities.LibraryRunLinkPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new jpa.entities.LibraryRunLinkPK();
            key.setIdLibrary(Integer.parseInt(values[0]));
            key.setIdRun(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(jpa.entities.LibraryRunLinkPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdLibrary());
            sb.append(SEPARATOR);
            sb.append(value.getIdRun());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof LibraryRunLink) {
                LibraryRunLink o = (LibraryRunLink) object;
                return getStringKey(o.getLibraryRunLinkPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + LibraryRunLink.class.getName());
            }
        }

    }

}
