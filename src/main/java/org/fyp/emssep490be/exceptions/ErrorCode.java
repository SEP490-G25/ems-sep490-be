package org.fyp.emssep490be.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "User not found"),
    USER_ALREADY_EXISTS(1002, "User already exists"),
    PASSWORD_TOO_SHORT(1003, "Password must be at least 8 characters long"),
    CATEGORY_NAME_BLANK(1004, "Category name cannot be blank"),
    CATEGORY_ALREADY_EXISTS(1005, "Category already exists"),
    CATEGORY_ADD_FAILED(1006, "Failed to add category"),
    CATEGORY_NOT_FOUND(1007, "Category not found"),
    ORDER_ID_BLANK(1008, "Order id cannot be blank"),
    PRODUCT_ID_BLANK(1009, "Product id cannot be blank"),
    QUANTITY_INVALID(1010, "Quantity must be greater than 0"),
    ORDER_INVALID(1011, "Order not found"),
    CUSTOMER_NAME_BLANK(1012, "Customer name cannot be blank"),
    CUSTOMER_PHONE_BLANK(1013, "Customer phone cannot be blank"),
    CUSTOMER_ADDRESS_BLANK(1014, "Customer address cannot be blank"),
    CUSTOMER_EMAIL_BLANK(1015, "Customer email cannot be blank"),
    CUSTOMER_EMAIL_INVALID(1016, "Invalid email format"),
    CUSTOMER_NOT_FOUND(1017, "Customer not found"),
    PRODUCT_NAME_BLANK(1018, "Product name cannot be blank"),
    PRODUCT_ALREADY_EXISTS(1019, "Product already exists"),
    PRODUCT_ADD_FAILED(1020, "Failed to add product"),
    WAREHOUSE_INVALID(1038, "Warehouse not found"),
    WAREHOUSE_NAME_BLANK(1021, "Warehouse name cannot be blank"),
    WAREHOUSE_ALREADY_EXISTS(1022, "Warehouse already exists"),
    WAREHOUSE_ADD_FAILED(1023, "Failed to add warehouse"),
    WAREHOUSE_NOT_FOUND(1024, "Warehouse not found"),
    SHIPMENT_ID_BLANK(1025, "Shipment id cannot be blank"),
    SHIPMENT_NOT_FOUND(1026, "Shipment not found"),
    SHIPMENT_ADD_FAILED(1027, "Failed to add shipment"),
    SHIPMENT_PRODUCT_ADD_FAILED(1028, "Failed to add shipment product"),
    SHIPMENT_PRODUCT_NOT_FOUND(1029, "Shipment product not found"),
    SHIPMENT_PRODUCT_ID_BLANK(1030, "Shipment product id cannot be blank"),
    SHIPMENT_PRODUCT_QUANTITY_INVALID(1031, "Shipment product quantity must be greater than 0"),
    SHIPMENT_PRODUCT_QUANTITY_NOT_ENOUGH(1032, "Shipment product quantity not enough"),
    SHIPMENT_PRODUCT_QUANTITY_EXCEED(1033, "Shipment product quantity exceed"),
    SHIPMENT_PRODUCT_ALREADY_EXISTS(1034, "Shipment product already exists"),
    SHIPMENT_PRODUCT_UPDATE_FAILED(1035, "Failed to update shipment product"),
    SHIPMENT_PRODUCT_DELETE_FAILED(1036, "Failed to delete shipment product"),
    SHIPMENT_PRODUCT_DELETE_NOT_FOUND(1037, "Shipment product to delete not found"),
    WAREHOUSE_ADDRESS_ALREADY_EXISTS(1039, "Warehouse address already exists"),
    PRODUCT_NOT_FOUND(1040, "Product not found"),
    PROVIDER_NAME_BLANK(1041, "Provider not found"),
    PROVIDER_ALREADY_EXISTS(1042, "Provider already exists"),
    PROVIDER_ADD_FAILED(1043, "Failed to add provider"),
    PROVIDER_NOT_FOUND(1044, "Provider not found"),
    EMAIL_ALREADY_EXISTS(1045, "Email already exists"),
    PHONE_ALREADY_EXISTS(1046, "Phone already exists"),
    ORDER_ADD_FAILED(1047, "Order added unsuccessfully"),
    ORDER_NOT_FOUND(1048, "Order not found"),
    ORDER_ALREADY_EXISTS(1049, "Order exists"),
    WAREHOUSE_NAME_EXISTS(1050, "Warehouse name already exists"),
    WAREHOUSE_ADDRESS_EXISTS(1051, "Warehouse address already exists"),
    ZONE_NOT_FOUND(1052, "Zone not found"),
    ZONE_EMPTY(1053, "Zone is empty"),
    ZONE_NAME_BLANK(1054, "Zone name cannot be blank"),
    ZONE_ALREADY_EXISTS(1055, "Zone already exists"),
    IMPORT_ADD_FAILED(1056, "Import added unsuccessfully"),
    IMPORT_NOT_FOUND(1057, "Import not found"),
    EXPORT_ADD_FAILED(1058, "Export added unsuccessfully"),
    EXPORT_NOT_FOUND(1059, "Export not found"),
    IMPORT_DETAIL_NOT_FOUND(1060, "Import detail not found"),
    EXPORT_DETAIL_NOT_FOUND(1060, "Export detail not found"),
    INVENTORY_NOT_FOUND(1061, "Inventory not found"),
    ZONE_FULL(1062, "Zone is full"),
    INSUFFICIENT_INVENTORY(1063, "Product quantity is insufficient"),
    PRODUCT_NOT_ENOUGH_IN_WAREHOUSE(1064, "Product quantity is not enough in warehouse"),
    OUT_OF_STOCK(1065, "Product not available in the warehouse"),
    OUT_OF_QUANTITY(1066, "Product quantity is not enough"),
    INVALID_STATUS_TO_EXPORT(1067, "Only pending exports can be processed"),
    WAREHOUSE_NOT_EMPTY(1068, "Warehouse is not empty");
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
