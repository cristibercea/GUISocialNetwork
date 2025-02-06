package ubb.scs.map.guisocialnetwork.utils;

import ubb.scs.map.guisocialnetwork.controller.MessageAlert;

public class ProblemReporter {
    public static void report(String message){
        MessageAlert.showErrorMessage(null,message);
    }
}
