//@DECLARE@
package net.casnw.home.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import net.casnw.home.meta.ModuleMetaObj;
import net.casnw.home.meta.VariableMetaObj;
import net.casnw.home.run.RedisUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 抽象模块
 *
 * @author myf@lzb.ac.cn
 * @since 2013-04-10
 * @version 1.0
 *
 */
public class AbsComponent implements Componentable {

    private Contextable _context = null;
    private Modelable _model = null;
    private String _name = getClass().getName();
    private String _url = "";
    private RestTemplate restTemplate = new RestTemplate();
    private RedisUtil redisUtil;    
    public String state = "create";
    
    
    ModuleMetaObj mmo;
    public AbsComponent() {
    //	this.mmo = mmo;
    	
    }
    
    public void setModuleMetaObject(JSONObject json) 
    {
    	
    	mmo.setModuleClass(json.getString("moduleClass"));
    	mmo.setName(json.getString("name"));
    	mmo.setAuthor(json.getString("author"));
    	mmo.setKeyword(json.getString("keyword"));
    	mmo.setDescription(json.getString("description"));
    	mmo.setTimeScale(json.getString("timeScale"));	
    	mmo.setSpaceScale(json.getString("spaceScale"));
    	mmo.setVersion(json.getString("version"));
    	mmo.setCategory(json.getString("category"));
    	mmo.setApplicationField(json.getString("applicationField"));
    	mmo.setTheory(json.getString("theory"));
    	mmo.setSpaRefSys(json.getString("spaRefSys"));
    	
    	
    	JSONArray varArray = json.getJSONArray("variables");
    	Iterator<JSONObject> it = varArray.iterator();
    	while(it.hasNext()) 
    	{
    		JSONObject item = (JSONObject)it.next();
    		VariableMetaObj vmo = new VariableMetaObj();
    		vmo.setDataType(item.getString("dataType"));
    		vmo.setUnit(item.getString("unit"));
    		vmo.setRange(item.getString("range"));
    		vmo.setValue(item.getString("value"));
    		vmo.setSize(item.getInt("size"));
    		vmo.setContext(item.getString("context"));
    		//vmo.setDate(item.getString("date"));
    		vmo.setName(item.getString("getName"));
    		vmo.setDescription(item.getString("description"));    		
    		mmo.addVarMetaObj(vmo);   
    		
    	}
     
    }
    
	  

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub

    	String url=_url+"/state/init";
    //	System.out.println("1111url="+_url);
    	List<AttributeAccess> aal = new ArrayList<AttributeAccess>();
    	JSONObject json = new JSONObject();
    	//json.put("compName", this._name);
    	//json.put("contextName", this._context.getInstanceName());
    	
        //List<Map> ttmaps=new ArrayList<>();
    	aal = this._context.getAttributeAccess();
    	for(AttributeAccess aa : aal) 
    	{
    		if(aa.getComponent() == this) {
    			Map<String,String> tts = new HashMap<String,String>();
    		//	System.out
			//	.println("111contextName=" + aa.getContext().getInstanceName() + "  attributeName=" + aa.getCompName() + " aa.getAttributeName()=" + aa.getAttributeName());

    			//模块中的名称，context中的名称
    			json.put(aa.getCompName(), aa.getContext().getInstanceName()+":"+aa.getAttributeName());	
    			//ttmaps.add(tts);
    		}
    	}
    	//json.put("variableName", ttmaps);
    	
        //headers
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap();
        requestBody.put("mapTable", json.toString());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        String s = restTemplate.postForObject(url+"?mapTable={mapTable}", requestEntity,String.class,requestBody);
  //      System.out.println("1122="+s);
    	state = "init";
    	
    }
    @Override
    public void run() throws Exception {
        // TODO Auto-generated method stub
    	//restTemplate.getForObject(_url+"/run/", String.class);
    	String url=_url+"/state/run";
    //	System.out.println("1111url="+url);
    	List<AttributeAccess> aal = new ArrayList<AttributeAccess>();
    	JSONObject json = new JSONObject();
    	/*if(url.contains("Spatial_flag"))
    	{
    		json.put("cols",((SpatialContext)_context).getCurrentColNum());
    		json.put("rows",((SpatialContext)_context).getCurrentRowNum());
     	}*/
    	
    	    	
        //headers
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap();
        
        requestBody.put("mapTable", json.toString());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        String s = restTemplate.postForObject(url+"?mapTable={mapTable}", requestEntity,String.class,requestBody);

   //     String s = restTemplate.postForObject(url, requestEntity,String.class,requestBody);
  //      System.out.println("1133="+s);
    	state = "run";
    }

    @Override
    public void clear() throws Exception {
        // TODO Auto-generated method stub
    	//restTemplate.getForObject(_url+"/clear/", String.class);
    	state = "clear";
    }

    @Override
    public Modelable getModel() {
        // TODO Auto-generated method stub

        return this._model;

    }

    @Override
    public Contextable getContext() {
        // TODO Auto-generated method stub
        return this._context;
    }

    @Override
    public void setModel(Modelable model) {
        if (model != null) {
            _model = model;
        }
    }

    @Override
    public void setContext(Contextable context) {
        if (context != null) {
            _context = context;
        }

    }

    @Override
    public void setInstanceName(String name) {
        _name = name;
    }

    @Override
    public String getInstanceName() {
        return _name;
    }
    
    @Override
    public String getMetadata() 
    { 	
    	JSONObject json = new JSONObject();
        json.put("moduleClass", mmo.getModuleClass());
        json.put("name", mmo.getName());
        json.put("author", mmo.getAuthor());
        json.put("keyword", mmo.getKeyword());
        json.put("description", mmo.getDescription());
        json.put("timeScale", mmo.getTimeScale());
        json.put("spaceScale", mmo.getSpaceScale());
        json.put("version", mmo.getVersion());
        json.put("category", mmo.getCategory());
        json.put("applicationField", mmo.getApplicationField());
        json.put("theory", mmo.getTheory());
        json.put("spaRefSys", mmo.getSpaRefSys());
        
        List<VariableMetaObj> vmlist = mmo.getVarMetaObjList();
        
        
        List<Map> ttmaps=new ArrayList<>();
        for (VariableMetaObj vmo:vmlist) {
        	Map<String,Object> tts = new HashMap<String,Object>();
        	tts.put("dataType",vmo.getDataType());
        	tts.put("description",vmo.getDescription());
        	tts.put("unit",vmo.getUnit());
        	tts.put("range",vmo.getRange());
        	tts.put("value",vmo.getValue());
        	tts.put("size",vmo.getSize());
        	tts.put("context", vmo.getContext());
        	tts.put("date",vmo.getDate());
        	tts.put("name", vmo.getName());
        	ttmaps.add(tts);
        	
        }
        json.put("variables", ttmaps);
        
         	
    	return json.toString();
    }
    
    @Override
    public String getInputAttributes() 
    {
   	
    	JSONObject json = new JSONObject();
        List<VariableMetaObj> vmlist = mmo.getVarMetaObjList();       
        List<Map> ttmaps=new ArrayList<>();
        for (VariableMetaObj vmo:vmlist) {
        	if(vmo.getType().startsWith("In")) {
	        	Map<String,Object> tts = new HashMap<String,Object>();
	        	tts.put("name", vmo.getName());
	        	tts.put("dataType",vmo.getDataType());
	        	tts.put("description",vmo.getDescription());
	        	tts.put("unit",vmo.getUnit());
	        	tts.put("range",vmo.getRange());
	        	tts.put("value",vmo.getValue());
	        	tts.put("size",vmo.getSize());
	        	tts.put("context", vmo.getContext());
	        	tts.put("date",vmo.getDate());
	        	ttmaps.add(tts);
        	}
        }
        json.put("variables", ttmaps);
        
         	
    	return json.toString();
    }
    
    @Override
    public String getOutputAttributes() 
    {
    
    	JSONObject json = new JSONObject();
        List<VariableMetaObj> vmlist = mmo.getVarMetaObjList();       
        List<Map> ttmaps=new ArrayList<>();
        for (VariableMetaObj vmo:vmlist) {
        	if(vmo.getType().endsWith("Out")) {
	        	Map<String,Object> tts = new HashMap<String,Object>();
	        	tts.put("name", vmo.getName());
	        	tts.put("dataType",vmo.getDataType());
	        	tts.put("description",vmo.getDescription());
	        	tts.put("unit",vmo.getUnit());
	        	tts.put("range",vmo.getRange());
	        	tts.put("value",vmo.getValue());
	        	tts.put("size",vmo.getSize());
	        	tts.put("context", vmo.getContext());
	        	tts.put("date",vmo.getDate());
	        	ttmaps.add(tts);
        	}
        }
        json.put("variables", ttmaps);
        
         	
    	return json.toString();
    } 
    
    @Override
    public String getAttributeValue(String myinput) 
    {
    	
      	String name = this.getContext().getInstanceName()+"."+myinput;
    	Object value = redisUtil.get(name);
    	if(value!=null) 
    	{
    		value=value.toString();
    	}

      System.out.println("get input value from redis="+ redisUtil.get(name).toString());
    	
        return value.toString();
    } 
    
       
    @Override
    public void setAttributeValue(String name,String value) 
    {	
    	
    	restTemplate.getForObject(_url+"/inputs/set?t={}&v={}", String.class,name,value);	
		this.redisUtil.set(this.getContext().getInstanceName(), value);  
        System.out.print("set successful");
    	    	
    }  
    

    @Override
    public String getState() 
    {
    	return _name;
    }
    
    @Override
    public void setState(String state, int value)
    {
    	
    }
    
    @Override
    public RedisUtil getRedisUtil() 
    {
    	return redisUtil;
    } 
    
    public void setRedisUtil(RedisUtil rs) 
    {
    	redisUtil = rs;
    }
    
    
    public String getURL()
    {
    	return _url;
    }
    
    public void setURL(String url)
    {
    	_url = url;
    }
    
    public void initValue(String value) {
    	
       	List<VariableMetaObj> vmlist = mmo.getVarMetaObjList(); 
    	List<String> inputVariableList = new ArrayList<String>(); 
    	for (VariableMetaObj vmo:vmlist) {
         	if(vmo.getType().startsWith("In")) {
 	        	String name = vmo.getName();
 	        	inputVariableList.add(name);
         	}
         }
    	
    	
    	value="{\"nhru\":\"23\",\"nsol\":\"100\"}";

    	JSONObject json = JSONObject.fromObject(value);
    	
    	for(String variableName:inputVariableList) 
    	{
	    		String variableValue = json.optString(variableName);
	    	//	System.out.println("variableVaule="+variableValue);
	    		if(!variableValue.isEmpty()) {	    	
	    			setAttributeValue(variableName,variableValue);
	    		//	System.out.println("success set="+variableValue);
	    		}
    	}

    }
    

}
