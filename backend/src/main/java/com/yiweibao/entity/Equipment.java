package com.yiweibao.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String model;

    @Column(length = 200)
    private String spec;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String workshop;

    @Column(length = 50)
    private String manager;

    private Double ratedSpindleSpeed;  // 额定主轴转速 rpm
    private Double ratedPower;         // 额定功率 kW
    private Double ratedCurrent;       // 额定电流 A
    private Double normalTempMax;      // 正常运行温度上限 °C

    private LocalDate purchaseDate;

    private LocalDate startDate;

    @Column(nullable = false)
    private Integer status; // 0-正常 1-待维修 2-维修中 3-已报废

    @Column(length = 500)
    private String qrCodePath;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public Equipment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Double getRatedSpindleSpeed() { return ratedSpindleSpeed; }
    public void setRatedSpindleSpeed(Double v) { this.ratedSpindleSpeed = v; }
    public Double getRatedPower() { return ratedPower; }
    public void setRatedPower(Double v) { this.ratedPower = v; }
    public Double getRatedCurrent() { return ratedCurrent; }
    public void setRatedCurrent(Double v) { this.ratedCurrent = v; }
    public Double getNormalTempMax() { return normalTempMax; }
    public void setNormalTempMax(Double v) { this.normalTempMax = v; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
