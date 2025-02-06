module ubb.scs.map.guisocialnetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.desktop;
    requires jbcrypt;

    opens ubb.scs.map.guisocialnetwork to javafx.fxml;
    opens ubb.scs.map.guisocialnetwork.controller to javafx.fxml;
    exports ubb.scs.map.guisocialnetwork;
    exports ubb.scs.map.guisocialnetwork.services;
    exports ubb.scs.map.guisocialnetwork.repository.dbrepo;
    exports ubb.scs.map.guisocialnetwork.controller;
    exports ubb.scs.map.guisocialnetwork.utils.observer;
    exports ubb.scs.map.guisocialnetwork.utils.events;
    exports ubb.scs.map.guisocialnetwork.utils;
    exports ubb.scs.map.guisocialnetwork.repository;
    exports ubb.scs.map.guisocialnetwork.domain.entities;
    exports ubb.scs.map.guisocialnetwork.domain.validators;
}