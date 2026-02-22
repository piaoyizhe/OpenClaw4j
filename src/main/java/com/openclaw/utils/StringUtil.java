//package com.openclaw.utils;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.JSONPath;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * @preserve public
// */
//public class StringUtil {
//	/**
//	 * format string
//	 */
//	public static String formatStringTrim(Object obj){
//		if(obj==null)return "";
//		String temp = obj.toString().trim();
//		if("".equals(temp) || "null".equals(temp)){
//			return "";
//		}
//		return temp;
//	}
//	/**
//	 * format string
//	 */
//	public static String ToString(Object obj){
//		if(obj==null)return "";
//		String temp = obj.toString().trim();
//		return temp;
//	}
//	public static String formatStringNotTrim(Object obj){
//		if(obj==null)return "";
//		String temp = obj.toString();
//		if("".equals(temp) || "null".equals(temp)){
//			return "";
//		}
//		return temp;
//	}
//
//	/**
//	 *  将下划线字段转换成 驼峰命名
//	 * @param input
//	 * @return
//	 */
//	public static String underscoreToCamelCase(String input) {
//		Pattern pattern = Pattern.compile("_(\\w)");
//		Matcher matcher = pattern.matcher(input);
//		StringBuffer result = new StringBuffer();
//
//		while (matcher.find()) {
//			matcher.appendReplacement(result, matcher.group(1).toUpperCase());
//		}
//		matcher.appendTail(result);
//
//		return result.toString();
//	}
//
//	/**
//	 * 判断字符串数组 String[]是否为空
//	 */
//	public static boolean isStringArrayEmpty(Object obj){
//		if(obj==null)return true;
//		try{
//			String[] strs = (String[])obj;
//			if(strs.length>0)
//				return false;
//			else
//				return true;
//		}catch(Exception e){
//			return true;
//		}
//	}
//	public static boolean isNotStringArrayEmpty(Object obj){
//		return !isStringArrayEmpty(obj);
//	}
//	/**
//	 * 判断是否为空
//	 */
//	public static boolean isEmpty(Object obj){
//		if(obj==null)
//			return true;
//		String temp = obj.toString().trim();
//		if("".equals(temp) || "null".equals(temp))
//			return true;
//		else
//			return false;
//	}
//	/**
//	 * 判断是否不为空
//	 */
//	public static boolean isNotEmpty(Object obj){
//		return !isEmpty(obj);
//	}
//	/**
//	 * 获取唯一数
//	 */
//	public static String  getDateRandow(){
//		SimpleDateFormat tempDate = new SimpleDateFormat("yyMMdd" + "" + "hhmmssSS");
//		String datetime = tempDate.format(new Date());    //12位
//		int randomInt = (int)(Math.random()*10000);
//		datetime =  datetime+randomInt;
//		return datetime;
//	}
//
//	/**
//	 * 生成大写数字
//	 */
//	public static String getBigWriteForTicket(double ap){
//		String temp = "00000"+String.valueOf(ap)+"0";
//		String[] strs =temp.split("\\.");
//		temp = strs[0].substring(strs[0].length()-6, strs[0].length())+strs[1].substring(0, 2);
//		char[] cha = temp.toCharArray();
//		String[] str = new String[8];
//		for(int i=0;i<8;i++){
//			if(cha[i]=='0')str[i]="零";
//			else if(cha[i]=='1')str[i]="壹";
//			else if(cha[i]=='2')str[i]="贰";
//			else if(cha[i]=='3')str[i]="叁";
//			else if(cha[i]=='4')str[i]="肆";
//			else if(cha[i]=='5')str[i]="伍";
//			else if(cha[i]=='6')str[i]="陆";
//			else if(cha[i]=='7')str[i]="柒";
//			else if(cha[i]=='8')str[i]="捌";
//			else str[i]="玖";
//		}
//		return "<b>ⓧ</b>"+str[0]+"<b>拾</b>&nbsp;"+str[1]+"<b>万</b>&nbsp;"+str[2]+"<b>仟</b>&nbsp;"
//		+str[3]+"<b>佰</b>&nbsp;"+str[4]+"<b>拾</b>&nbsp;"+str[5]+"<b>元</b>&nbsp;"+str[6]+"<b>角</b>&nbsp;"+str[7]+"<b>分</b>";
////				"￥:</b>&nbsp;"+ap;
//
//	}
//
//	  /**
//		  * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的"/"符号。
//		  *
//		  * @param path 文件路径
//		  * @return 格式化后的文件路径
//		  */
//		  public static String formatPath(String path) {
//		    String reg0 = "\\\\＋";
//		    String reg = "\\\\＋|/＋";
//		    String temp = path.trim().replaceAll(reg0, "/");
//		    temp = temp.replaceAll(reg, "/");
//		    if (temp.endsWith("/")) {
//		        temp = temp.substring(0, temp.length() - 1);
//		    }
//		    if (System.getProperty("file.separator").equals("\\")) {
//		      temp= temp.replace('/','\\');
//		    }
//		    return temp;
//		  }
//
//	/**
//	 * 前面补0 ，默认共6位数
//	 * @param obj
//	 * @param pattern
//	 * @return
//	 */
//	public static String beforeAdd0(Object obj,String... pattern){
//		if(obj!=null){
//			if(pattern==null || pattern.length==0)
//				pattern = new String[]{"0000000"} ;
//			 java.text.DecimalFormat df = new java.text.DecimalFormat(pattern[0]);
//			 return df.format(obj).toString() ;
//		}else
//			return null;
//	}
//
//
//
//	/**
//	 * 去除前缀，一般用于表名 sys_u_user_info  sys_u_ --> user_info
//	 * @param str
//	 * @param prefix 前缀分隔字符
//	 * @return
//	 */
//	public static String removePrefix(String str,String prefix){
//		String tmp=null;
//		if(str!=null && !"".equals(str)){
//			if(prefix!=null && !"".equals(prefix)){
//				String[] prefixs = prefix.split(",");
//				for(int i=0,n=prefixs.length;i<n;i++){
//					if(str.startsWith(prefixs[i])){
//						tmp =str.substring(str.indexOf(prefixs[i])+prefixs[i].length());
//						break;
//					}
//				}
//
//			}else
//				tmp = str;
//		}
//		return tmp;
//	}
//	/**
//	 * class 属性规范, finger_code --> fingerCode
//	 * @param str
//	 * @param type
//	 * @return
//	 */
//	public static String removeSplit(String str,String type){
//		String tmp=null;
//		if(str!=null && !"".equals(str)){
//			StringBuffer sf = new StringBuffer();
//			str = str.toLowerCase();
//			String[] strs = str.split(type);
//			if(strs!=null && strs.length>0){
//				for(int i=0,n=strs.length;i<n;i++){
//					if(i>0){
//						sf.append(firstUpperCase(strs[i]));
//					}else
//						sf.append(strs[i]);
//				}
//			}else{
//				sf.append(str);
//			}
//			tmp = sf.toString();
//		}
//		return tmp;
//	}
//	/**
//	 * 字符首位大写
//	 * @param str
//	 * @return
//	 */
//	public static String firstUpperCase(String str){
//		String tmp=null;
//		if(str!=null && !"".equals(str)){
//			tmp = str.substring(0,1).toUpperCase().concat(str.substring(1));
//		}
//		return tmp;
//	}
//	/**
//	 * 字符首位小写
//	 * @param str
//	 * @return
//	 */
//	public static String firstLowerCase(String str){
//		String tmp=null;
//		if(str!=null && !"".equals(str)){
//			tmp = str.substring(0,1).toLowerCase().concat(str.substring(1));
//		}
//		return tmp;
//	}
//
//
//	/**
//	 * 获取占位符内的内容
//	 * @param str
//	 * @param regex
//	 * @return
//	 */
//	public static List<String> getPlaceholder(String str, String regex){
//		Pattern pattern=Pattern.compile(regex);
//		Matcher matcher=pattern.matcher(str);
//		List<String> keyList=new ArrayList<>();
//
//		while (matcher.find()){
//			keyList.add(matcher.group());
//		}
//		return keyList;
//	}
//
//	/**
//	 * 使用 JSONObject 内的内容替换模板内容
//	 * @param template	模板内容
//	 * @param jsonObject	数据
//	 * @param placeholderPattern	占位符
//	 * @return
//	 */
//	public static String replacePlaceholders(String template, JSONObject jsonObject, String placeholderPattern) {
//		Pattern pattern = Pattern.compile(placeholderPattern);
//		Matcher matcher = pattern.matcher(template);
//
//		StringBuffer buffer = new StringBuffer();
//		while (matcher.find()) {
//			String key = matcher.group(1);
//			Object value = JSONPath.eval(jsonObject, "$." + key);
//			String replacement="";
//			if (value instanceof JSONObject ||value instanceof JSONArray) {
//				// obj 是一个 JSON 对象 或者 是一个 JSON 数组
//				replacement=JSONObject.toJSONString(value);
//			} else{
//				replacement=String.valueOf(value);
//			}
////			String replacement = JSONObject.toJSONString(JSONPath.eval(jsonObject, "$."+key));
//			matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement != null ? replacement : ""));
//		}
//		matcher.appendTail(buffer);
//		return buffer.toString();
//	}
//
//
//	/**
//	 * 替换占位符 & 替换变量
//	 *
//	 * @param template 模板
//	 * @param requestBody	请求数据
//	 * @param responseData	系统响应数据
//	 * @param dataObject	业务返回数据
//	 * @return
//	 */
//	public static String replacePlaceholders(String template,JSONObject requestBody,JSONObject responseData,JSONObject dataObject){
//		//读取请求参数内容值
//		if (template.contains("${request.")) {
//			template = StringUtil.replacePlaceholders(template, requestBody, "\\$\\{request\\.(.+?)\\}");
//		}
//		//读取网络请求内容值
//		if (template.contains("${res.")) {
//			template = StringUtil.replacePlaceholders(template, responseData, "\\$\\{res\\.(.+?)\\}");
//		}
//		//读取响应参数内容值
//		if (template.contains("${data.") && StringUtil.isNotEmpty(dataObject)) {
//			template = StringUtil.replacePlaceholders(template, dataObject, "\\$\\{data\\.(.+?)\\}");
//		}
//
//		//替换变量
//		template = template.replace("${time}", DateUtils.getCurrentTime("HH:mm")); //替换时间
//		template = template.replace("${datetime}", DateUtils.getCurrentTime()); //替换日期时间
//		template = template.replace("${date}", DateUtils.getCurrentTime("yyyy-MM-dd")); //替换日期
//		template = template.replace("${yesterday}", DateUtils.getYesterday()); //替换昨天日期
//		template = template.replace("${tomorrow}", DateUtils.getTomorrow()); //替换明天日期
//
//		return template;
//	}
//
//	public static Map<String, String> jsonToMap(JSONObject jsonObject) {
//		Map<String, String> resultMap = new HashMap<>();
//		populateMapFromJson(jsonObject, resultMap, "");
//		return resultMap;
//	}
//
//	private static void populateMapFromJson(JSONObject jsonObject, Map<String, String> resultMap, String parentKey) {
//		Set<String> keys = jsonObject.keySet();
//		for (String key : keys) {
//			Object value = jsonObject.get(key);
//			String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;
//			if (value instanceof JSONObject) {
//				populateMapFromJson((JSONObject) value, resultMap, fullKey);
//			} else {
//				resultMap.put(fullKey, value.toString());
//			}
//		}
//	}
//}