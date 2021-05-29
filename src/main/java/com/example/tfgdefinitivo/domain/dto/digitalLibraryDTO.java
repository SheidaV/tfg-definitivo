package com.example.tfgdefinitivo.domain.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "digitalLibrary")
public class digitalLibraryDTO implements Serializable{

    private static final long serialVersionUID = 1L;
    private int idDL;
    private String name;
    private String url;

    public digitalLibraryDTO(int idDL, String name, String url){
        this.idDL = idDL;
        this.name = name;
        this.url = url;
    }
    public int getIdDL() {
        return idDL;
    }
    @XmlElement
    public void setidDL(int idDL) {
        this.idDL = idDL;
    }
    public String getName() {
        return name;
    }
    @XmlElement
    public void setName(String name) {
        this.name = name;
    }
    public String geturl() {
        return url;
    }
    @XmlElement
    public void seturl(String url) {this.url = url; }

}