package jsf;

import jpa.entities.FieldReport;
import jsf.util.JsfUtil;
import jsf.util.PaginationHelper;
import jpa.session.FieldReportFacade;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("fieldReportController")
@SessionScoped
public class FieldReportController implements Serializable {

    private FieldReport current;
    private DataModel items = null;
    @EJB
    private jpa.session.FieldReportFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    

    
    
     //paso de variable a otra vista   ----- Inicio
    
    private int varIdReportProject;

    public int getVarIdReportProject() {
        return varIdReportProject;
    }

    public void setVarIdReportProject(int varIdReportProject) {
        this.varIdReportProject = varIdReportProject;
    }
    //paso de variable a otra vista   ----- Inicio
    
    //
    

    public FieldReportController() {
    }

    public FieldReport getSelected() {
        if (current == null) {
            current = new FieldReport();
            selectedItemIndex = -1;
        }
        return current;
    }

    private FieldReportFacade getFacade() {
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
    
    
    //Metodos personalizados Linabat  Inicio
    public String prueba1(){
        String mensaje;
        
        mensaje = "el id actual es: " + varIdReportProject;
        FacesMessage msj = new FacesMessage(mensaje);
        FacesContext.getCurrentInstance().addMessage(null, msj);
        
        return null;
    }
    //Metodos personalizados Linabat  Final

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (FieldReport) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new FieldReport();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundlefield").getString("FieldReportCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundlefield").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (FieldReport) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundlefield").getString("FieldReportUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundlefield").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (FieldReport) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundlefield").getString("FieldReportDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundlefield").getString("PersistenceErrorOccured"));
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

    public FieldReport getFieldReport(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = FieldReport.class)
    public static class FieldReportControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FieldReportController controller = (FieldReportController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "fieldReportController");
            return controller.getFieldReport(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof FieldReport) {
                FieldReport o = (FieldReport) object;
                return getStringKey(o.getIdFieldReport());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + FieldReport.class.getName());
            }
        }

    }

}
