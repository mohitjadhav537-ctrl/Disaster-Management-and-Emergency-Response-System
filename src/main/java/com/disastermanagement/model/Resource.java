package com.disastermanagement.model;

/**
 * Model representing relief inventory items.
 * Implements Reportable.
 */
public class Resource implements Reportable {
    private int resourceId;
    private String resourceName;
    private String category; // Food, Water, Medical, Rescue Equipment, Shelter Supplies, Clothing, Other
    private int quantity;
    private String unit;
    private String storageLocation;
    private int minimumStock;

    public Resource() {
        this.quantity = 0;
        this.minimumStock = 10;
    }

    public Resource(String resourceName, String category, int quantity, String unit, String storageLocation, int minimumStock) {
        this.resourceName = resourceName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.storageLocation = storageLocation;
        this.minimumStock = minimumStock;
    }

    public Resource(int resourceId, String resourceName, String category, int quantity, String unit, String storageLocation, int minimumStock) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.storageLocation = storageLocation;
        this.minimumStock = minimumStock;
    }

    public boolean isLowStock() {
        return quantity <= minimumStock;
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Resource ID", "Resource Name", "Category", "Available Qty", "Unit", "Storage Location", "Min Stock Alert"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{resourceId, resourceName, category, quantity, unit, storageLocation, minimumStock};
    }

    // Getters and Setters
    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
    }

    @Override
    public String toString() {
        return String.format("%s (%d %s) [%s]", resourceName, quantity, unit, category);
    }
}
