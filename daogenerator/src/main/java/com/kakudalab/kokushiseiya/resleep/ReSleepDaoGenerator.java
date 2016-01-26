package com.kakudalab.kokushiseiya.resleep;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class ReSleepDaoGenerator {
    public static void main(String args[]) throws Exception{
        Schema schema = new Schema(1, "greendao");
        Entity memo = schema.addEntity("Memo");
        memo.addIdProperty();
        memo.addStringProperty("text");
        memo.addLongProperty("date");
        Entity heartRate = schema.addEntity("Heart");
        heartRate.addIdProperty();
        heartRate.addDoubleProperty("hz");
        heartRate.addDoubleProperty("pow");
        heartRate.addDoubleProperty("heatRate");
        heartRate.addLongProperty("date");
        Entity user = schema.addEntity("User");
        user.addIdProperty();
        user.addStringProperty("text");
        user.addLongProperty("date");
        user.addStringProperty("category");
        Entity graph = schema.addEntity("Graph");
        graph.addIdProperty();
        graph.addDoubleProperty("value");
        Entity recommend = schema.addEntity("Recommend");
        recommend.addIdProperty();
        recommend.addStringProperty("text");
        recommend.addBooleanProperty("evaluation");
        recommend.addLongProperty("date");
        Entity sensorData = schema.addEntity("Sensor");
        sensorData.addIdProperty();
        sensorData.addDoubleProperty("value");
        Entity flag = schema.addEntity("DataFlag");
        flag.addIdProperty();
        flag.addBooleanProperty("is");
        Entity stanford = schema.addEntity("Stanford");
        stanford.addIdProperty();
        stanford.addLongProperty("date");
        stanford.addIntProperty("evaluation");
        Entity decideAction = schema.addEntity("DecideAction");
        decideAction.addIdProperty();
        decideAction.addStringProperty("action");
        decideAction.addBooleanProperty("evaluation");
        decideAction.addLongProperty("date");
        new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema, args[0]);
    }
}
