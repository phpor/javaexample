package phpor.json;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import java.util.HashMap;
import java.util.Map;


public class Test {
    public static void main(String[] args) {


        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("name", "phpor");

        JSONArray ja1 = JSONArray.fromObject(map1);

        String json_obj = ja1.toString();
        String json_str = JSONUtils.quote(ja1.toString());

        System.out.println();

        System.out.println(json_obj);
        System.out.println(json_str + "\n");

        JSONObject jo1 = new JSONObject();
        jo1.put("str", json_str);
        jo1.put("obj", json_obj);

        System.out.println("期望看到的");
        System.out.println("{\"str\":\"[{\\\"name\\\":\\\"phpor\\\"}]\\\"}]\",\"obj\":[{\"name\":\"phpor\"}]}");
        System.out.println();
        System.out.println("实际看到的");

        System.out.println(JSONUtils.valueToString(jo1));
    }
}
