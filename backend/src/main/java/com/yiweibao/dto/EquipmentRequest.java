package com.yiweibao.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class EquipmentRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String model;
    private String spec;
    private String manufacturer;
    private String location;
    private String workshop;
    private String manager;
    private LocalDate purchaseDate;
    private LocalDate startDate;
    private Integer status = 0;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWorkshop() { return workshop; }
    public void setWorkshop(String workshop) { this.workshop = workshop; }
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
