package com.jin.env.garbage.utils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class JSONUtil {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    static {
        // 忽略属性
        objectMapper.setDateFormat(new SimpleDateFormat(TimeType.FORMAT_DATE_TIME));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 该特性决定是否接受强制非数组（JSON）值到Java集合类型。如果允许，集合反序列化将尝试处理非数组值。
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 该特性可以允许JSON空字符串转换为POJO对象为null。如果禁用，则标准POJO只会从JSON null或者JSON对象转换过来；
        // 如果允许，则空JSON字符串可以等价于JSON null
        objectMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    }

    private JSONUtil() {

    }

    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    /**
     * javaBean,list,array convert to json string
     *
     * @throws JsonProcessingException
     */
    public static String obj2json(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * json string convert to javaBean
     */
    public static <T> T json2pojo(String jsonStr, Class<T> clazz) throws Exception {
        return objectMapper.readValue(jsonStr, clazz);
    }

    /**
     * json string convert to map
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, Object> json2map(String jsonStr) throws Exception {
        return objectMapper.readValue(jsonStr, Map.class);
    }

    /**
     * json string convert to map with javaBean
     */
    public static <T> Map<String, T> json2map(String jsonStr, Class<T> clazz) throws Exception {
        Map<String, Map<String, Object>> map = objectMapper.readValue(jsonStr, new TypeReference<Map<String, T>>() {
        });
        Map<String, T> result = new HashMap<String, T>();
        for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2pojo(entry.getValue(), clazz));
        }
        return result;
    }

    public static TreeMap<String, String> json2mapOne(String jsonStr) throws Exception {
        // logger.info(jsonStr);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(jsonStr, Map.class);

        TreeMap<String, String> result = new TreeMap<String, String>();
        for (Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }
        return result;
    }

    /**
     * json array string convert to list with javaBean
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) throws Exception {
        List<Map<String, Object>> list = objectMapper.readValue(jsonArrayStr, new TypeReference<List<T>>() {
        });
        List<T> result = new ArrayList<T>();
        for (Map<String, Object> map : list) {
            result.add(map2pojo(map, clazz));
        }
        return result;
    }

    /**
     * map convert to javaBean
     */
    public static <T> T map2pojo(Map<?, ?> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

    /**
     * 判断字符串是否是JSON字符串
     *
     * @param str
     * @return
     * @created
     * @author
     */
    // public static boolean isJsonString(String str) {
    // return isJsonObjectString(str) || isJsonArrayString(str);
    // }

    /**
     * 判断字符串是否是JSON对象字符串
     *
     * @param str
     * @return
     * @created
     * @author
     */
    // public static boolean isJsonObjectString(String str) {
    // return str != null && str.matches("^\\{.*\\}$");
    // }

    /**
     * 判断字符串是否是JSON数组字符串
     *
     * @param st
     * @return
     * @created
     * @author
     */
    // public static boolean isJsonArrayString(String str) {
    // return str != null && str.matches("^\\[.*\\]$");
    // }

    public static <T> T jsonToPojoWithDate(String jsonStr, Class<T> clazz, String format) throws Exception {
        if (StringUtils.isBlank(format))
            format = TimeType.FORMAT_DATE_TIME;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(format));
        return mapper.readValue(jsonStr, clazz);

    }

    public static ObjectMapper setMinObjectMapper(ObjectMapper mapper, Class<?> target, Class<?> min) {
        mapper.addMixIn(target, min);
        return mapper;

    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 该特性决定是否接受强制非数组（JSON）值到Java集合类型。如果允许，集合反序列化将尝试处理非数组值。
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 该特性可以允许JSON空字符串转换为POJO对象为null。如果禁用，则标准POJO只会从JSON null或者JSON对象转换过来；
        // 如果允许，则空JSON字符串可以等价于JSON null
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.setDateFormat(new SimpleDateFormat(TimeType.FORMAT_DATE_TIME));
        return mapper;

    }

    public static SimpleFilterProvider WithFilter(SimpleFilterProvider filter, ObjectMapper mapper, String type,
                                                  Class<?> target, Class<?> min, String filterName, String... properties) {
        if (filter == null) {
            return new SimpleFilterProvider().setFailOnUnknownId(false);
        }

        if (type.equalsIgnoreCase("include"))
            // 获取想要属性
            filter.addFilter(filterName, SimpleBeanPropertyFilter.filterOutAllExcept(properties));

            // 排除属性
        else if (type.equalsIgnoreCase("exclude"))
            filter.addFilter(filterName, SimpleBeanPropertyFilter.serializeAllExcept(properties));
        else {
        }
        if (target != null)
            setMinObjectMapper(mapper, target, min);
        return filter;
    }

    /**
     * 从json树形中获取某个节点 内容
     *
     * @param data
     * @param nodename
     * @return
     * @throws Exception
     */
    public static String jsonToTree(String data, String nodename) throws Exception {
        JsonNode rootNode = objectMapper.readTree(data);
        return rootNode.path(nodename).toString();
    }



    @SuppressWarnings("unchecked")
    public static <T> T treeNameByType(JsonNode node, String name, Class<T> clazz) {
        if (node.has(name)) {
            if (clazz.equals(Double.class)) {
                return (T) Double.valueOf(node.get(name).asDouble());

            } else if (clazz.equals(Long.class)) {
                return (T) Long.valueOf(node.get(name).asLong());

            } else if (clazz.equals(String.class)) {

                return (T) String.valueOf(node.get(name).asText());
            } else if (clazz.equals(Integer.class))
                return (T) Integer.valueOf(node.get(name).asInt());
            else
                return (T) Boolean.valueOf(node.get(name).asBoolean());
        } else
            return null;
    }
    public interface TimeType {

        /** 常用日期格式常量 */
        public static final String FORMAT_YEAR = "yyyy";
        public static final String FORMAT_MONTH = "yyyy-MM";
        public static final String FORMAT_DATE = "yyyy-MM-dd";
        public static final String FORMAT_DATE_HOUR = "yyyy-MM-dd HH:00";
        public static final String FORMAT_DATE_MINUTE = "yyyy-MM-dd HH:mm";
        public static final String FORMAT_HOUR = "HH:00";
        public static final String FORMAT_MINUTE = "HH:mm";
        public static final String FORMAT_TIME = "HH:mm:ss";
        public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";

        /** 常用时间单位格式常量 */
        public static final String UFMT_YEAR = "y";
        public static final String UFMT_MONTH = "M";
        public static final String UFMT_DAY = "d";
        public static final String UFMT_HOUR = "H";
        public static final String UFMT_MINUTE = "m";
        public static final String UFMT_SECOND = "s";
        public static final String UFMT_MILLISECOND = "S";

    }
}