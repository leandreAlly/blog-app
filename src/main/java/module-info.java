module com.ally.blogapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.ally.blogapp to javafx.fxml;
    opens com.ally.blogapp.controller to javafx.fxml;
    exports com.ally.blogapp;
    exports com.ally.blogapp.model;
    exports com.ally.blogapp.dao;
    exports com.ally.blogapp.dao.impl;
    exports com.ally.blogapp.service;
    exports com.ally.blogapp.util;
}