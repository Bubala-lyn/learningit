package utils;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class ReadJSON {

    // 读取知识点
    public static List<String> knowledges() {
        List<String> labelKeys = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("knowledges.json")
			));
            JsonParser parser = new JsonParser(); //创建JSON解析器
            JsonObject object = (JsonObject) parser.parse(br); //创建JsonObject对象
            JsonArray array = object.get("RECORDS").getAsJsonArray(); //得到为json的数组
            
            for (int i = 0; i < array.size(); i++) {
                JsonObject subObject = array.get(i).getAsJsonObject();
                labelKeys.add(subObject.get("knowledgeId").getAsString());
            }

        } catch (Exception e) {
			e.printStackTrace();
		}
        return labelKeys;
    }

    // 读取知识点先验概率
    public static Map<String, Double> knowledgePro() {
        Map<String, Double> map = new HashMap<String, Double>();
        try {
            JsonReader br = new JsonReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("knowledgePro.json")
			));
            Type mapTokenType = new TypeToken<Map<String, Double>>(){}.getType();
            map = new Gson().fromJson(br, mapTokenType);
            
        } catch (Exception e) {
			e.printStackTrace();
		}
        return map;
    }

    // 在知识点下分词的条件概率
    public static Map<String, Double> wordCondPro() {
        Map<String, Double> map = new HashMap<String, Double>();
        try {
            JsonReader br = new JsonReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("wordCondPro.json")
			));
            Type mapTokenType = new TypeToken<Map<String, Double>>(){}.getType();
            map = new Gson().fromJson(br, mapTokenType);

        } catch (Exception e) {
			e.printStackTrace();
		}
        return map;
    }

    // adaboost 改良后的概率
    public static Map<String, Double> wordAdaboostPro() {
        Map<String, Double> map = new HashMap<String, Double>();
        try {
            JsonReader br = new JsonReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("wordAdaboostPro.json")
			));
            Type mapTokenType = new TypeToken<Map<String, Double>>(){}.getType();
            map = new Gson().fromJson(br, mapTokenType);
            
        } catch (Exception e) {
			e.printStackTrace();
		}
        return map;
    }

    // 读取测试数据
    public static List<String> contents() {
        List<String> contentList = new ArrayList<String>();
        try {
            JsonReader br = new JsonReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("contents.json")
			));
            Type tokenType = new TypeToken<List<String>>(){}.getType();
            contentList = new Gson().fromJson(br, tokenType);

        } catch (Exception e) {
			e.printStackTrace();
		}
        return contentList;
    }

    // 读取知识点id 和 name 的 map
    public static Map<String, String> knowledgesName() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("knowledges.json")
			));
            JsonParser parser = new JsonParser(); //创建JSON解析器
            JsonObject object = (JsonObject) parser.parse(br); //创建JsonObject对象
            JsonArray array = object.get("RECORDS").getAsJsonArray(); //得到为json的数组
            
            for (int i = 0; i < array.size(); i++) {
                JsonObject subObject = array.get(i).getAsJsonObject();
                map.put(subObject.get("knowledgeId").getAsString(), subObject.get("knowledgeName").getAsString());
            }

        } catch (Exception e) {
			e.printStackTrace();
		}
        return map;
    }

    // 读取latex 数学符号对照表
    public static Map<String, String> latexSymbolMap() {
        Map<String, String> map = new HashMap<String, String>();
        try {
            JsonReader br = new JsonReader(new InputStreamReader(
				CommonUtils.class.getClassLoader().getResourceAsStream("latexSymbol.json")
			));
            Type mapTokenType = new TypeToken<Map<String, String>>(){}.getType();
            map = new Gson().fromJson(br, mapTokenType);
            
        } catch (Exception e) {
			e.printStackTrace();
		}
        return map;
    }
}
