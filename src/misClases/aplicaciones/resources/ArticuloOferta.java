/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package misClases.aplicaciones.resources;

/**
 *
 * @author Francisco Portela Henche
 */
public class ArticuloOferta implements Comparable{
    private String nombreArticulo;
    private int estadoArticulo;
    
    public static final int ESTADO_MARCADO = 1;;
    public static final int ESTADO_SIN_MARCAR = 0;
    
    public ArticuloOferta(){
        this.setNombreArticulo("");
        this.setEstadoArticulo(ArticuloOferta.ESTADO_SIN_MARCAR);
    }
    
    public ArticuloOferta(String nombre, int estado){
        this.setNombreArticulo(nombre);
        this.setEstadoArticulo(estado);
    }
    
    public String getNombreArticulo(){
        return this.nombreArticulo;
    }
    
    public void setNombreArticulo(String nombre){
        this.nombreArticulo = nombre;
    }
    
    public int getEstadoArticulo(){
        return this.estadoArticulo;
    }
    
    public void setEstadoArticulo(int estado){
        this.estadoArticulo = estado;
    }
    
    public void cambiarEstadoArticuloOferta(){
        if(this.getEstadoArticulo() == ArticuloOferta.ESTADO_SIN_MARCAR){
            this.setEstadoArticulo(ArticuloOferta.ESTADO_MARCADO);
        }
        else{
            this.setEstadoArticulo(ArticuloOferta.ESTADO_SIN_MARCAR);
        }
    }

    @Override
    public String toString(){
        String texto = "";
        if(this.getEstadoArticulo() == ArticuloOferta.ESTADO_SIN_MARCAR){
            texto = this.getNombreArticulo();
        }
        else{
            texto = "<html><s>" + this.getNombreArticulo() + "</s></html>";
        }
        return texto;
    }
    
    public int compareTo(Object obj){
        ArticuloOferta articuloAComparar = (ArticuloOferta)obj;
        return this.getNombreArticulo().
                compareToIgnoreCase(articuloAComparar.getNombreArticulo());
    }
}
