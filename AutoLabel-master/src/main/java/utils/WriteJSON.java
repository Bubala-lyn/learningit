package utils;

import java.util.*;
import com.google.gson.*;
import utils.FileOperation;

public class WriteJSON {
    // 写入知识点先验概率
    public static void knowledgePro(Map<String, Double> content, int k) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(content);
        new FileOperation().write("knowledgePro" + k + ".json", gsonString);
    }
    // 在知识点下分词的条件概率
    public static void wordCondPro(Map<String, Double> content, int k) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(content);
        new FileOperation().write("wordCondPro" + k + ".json", gsonString);
    }
    // adaboost 改良后的概率
    public static void wordAdaboostPro(Map<String, Double> content, int k) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(content);
        new FileOperation().write("wordAdaboostPro" + k + ".json", gsonString);
    }
}
