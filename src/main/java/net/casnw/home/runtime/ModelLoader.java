/**
 * 加载模型
 *
 * @author zxr@lzb.ac.cn
 * @since 2013-04-15
 * @version 1.0
 *
 */
//@DECLARE@
package net.casnw.home.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.casnw.home.io.ContextDescription;
import net.casnw.home.io.ModelDescription;
import net.casnw.home.io.ModuleDescription;
import net.casnw.home.io.VariableDescription;
import net.casnw.home.model.AbsComponent;
import net.casnw.home.model.AttributeAccess;
import net.casnw.home.model.Componentable;
import net.casnw.home.model.Contextable;
import net.casnw.home.model.Model;
import net.casnw.home.run.RedisUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加载模型
 *
 * @author zxr@lzb.ac.cn
 * @since 2013-04-15
 * @version 1.0
 *
 */
@RestController
@EnableAutoConfiguration
public class ModelLoader {

	private Model _homeModel;
	protected final Log _logger = LogFactory.getLog(getClass());
	private Map<String, Contextable> _contextList = new HashMap<String, Contextable>();

	RedisUtil redisUtil;

	/**
	 *
	 * @param rt
	 */
	public ModelLoader(Runtime rt) {

		// create an empty model
		// rt.redisUtil.set("aa", "ddddd");

		// System.out.println("ssssssssssssssssss="+rt.redisUtil.get("aa"));
		redisUtil = rt.getRedisUtil();

		_homeModel = new Model();

		// this context refers to itself
		_homeModel.setModel(_homeModel);
		_homeModel.setContext(_homeModel);
		_homeModel.setRedisUtil(redisUtil);
		// _homeModel.setRuntime(rt); Model不需要拥有Runtime
	}

	/**
	 * 加载并返回一个新的模型
	 *
	 * @param modelDescription 模型描述树
	 * @return Model
	 */
	public Model loadModel(ModelDescription modelDescription)
			throws IllegalAccessException, ClassNotFoundException, InstantiationException, Exception {

		_logger.info("loadModel begin");

		List<ModuleDescription> componentList;
		List<VariableDescription> variableList;
		String contextClassName = "net.casnw.home.model.Model";
		AttributeAccess attribute;
		// 取得模型名 根级
		String modelName = modelDescription.getInstanceName();
		_logger.info("modelName1====" + modelName);
		variableList = modelDescription.getVariableList();
		Contextable model = (Contextable) loadComponent(contextClassName);

		if (modelName == null && "".equalsIgnoreCase(modelName)) {
			modelName = "model";
			_logger.info("modelName2====" + modelName);
		}
		model.setInstanceName(modelName);
		// _logger.info("_contextList.containsKey(" + modelName + ")===" +
		// _contextList.containsKey(modelName));
		if (getContextList().containsKey(modelName)) {
			throw new IllegalArgumentException("context [" + modelName + "]same name ,Please Note To Amend");
		} else {
			getContextList().put(modelName, model);// 将模型放入容器列表
		}
		_homeModel = (Model) model;

		for (int v = 0; v < variableList.size(); v++) {
			VariableDescription variableDes = variableList.get(v);

			attribute = paserVarDes(variableDes);
			attribute.setComponent(_homeModel);
			attribute.setContext(model);
			_homeModel.addAccess(attribute);
		}

		// 定义context
		componentList = modelDescription.getModuleList();// 取出所有context
		for (int i = 0; i < componentList.size(); i++) {
			ModuleDescription moduleDes = componentList.get(i);
			Contextable context = loadContext((ContextDescription) (moduleDes));
			_homeModel.addComponent(context);
		}
		_logger.info("loadModel end");
		return _homeModel;
	}

	/**
	 * 封装容器列表
	 *
	 * @param contextDes 容器
	 */
	private Contextable loadContext(ContextDescription contextDes) throws Exception {
		AbsComponent component;
		AttributeAccess attri = new AttributeAccess();

		String contextClassName = contextDes.getInstanceClass();// 取出context类
		VariableDescription contextVarDes;
		String contextName = contextDes.getInstanceName();// 取出context名
		if (contextName == null && "".equalsIgnoreCase(contextName)) {
			throw new IllegalArgumentException("context Name is null！");
		}
		_logger.info("contextName===" + contextName);
		Contextable context = (Contextable) loadComponent(contextClassName);
		context.setRedisUtil(redisUtil);
		context.setInstanceName(contextName);
		if (getContextList().containsKey(contextName)) {
			throw new IllegalArgumentException("context [" + contextName + "]same name ,Please Note To Amend");
		} else {
			getContextList().put(contextName, context);
		}
		List<VariableDescription> variableList = contextDes.getVariableList();// 取出context下变量列表
		for (int j = 0; j < variableList.size(); j++) {
			contextVarDes = variableList.get(j);
			attri = paserVarDes(contextVarDes);
			attri.setContext(context);
			context.addAccess(attri);
		}

		// 定义component
		List<ModuleDescription> componentList = contextDes.getModuleList();// 取出component列表
		ModuleDescription componentDes;
		String componentName;
		for (int j = 0; j < componentList.size(); j++) {
			componentDes = componentList.get(j);
			if (componentDes instanceof ContextDescription) {
				context.addComponent(loadContext((ContextDescription) componentDes));
			} else if (componentDes instanceof ModuleDescription) {
				// component解析过程

				component = componentInstance(componentDes);
				component.setContext(context);
				component.setModel(_homeModel);
				context.addComponent(component);
				componentName = componentDes.getInstanceName();

				// component.setRedisUtil(redisUtil);

				// 取出component下变量列表
				List<VariableDescription> componentvariableList = componentDes.getVariableList();
				VariableDescription varDes;
				attri = null;
				for (int k = 0; k < componentvariableList.size(); k++) {
					varDes = componentvariableList.get(k);
					attri = paserVarDes(varDes);
					if (varDes.getVariableContext() == null || "".equalsIgnoreCase(varDes.getVariableContext())) {
						attri.setContext(context);
					}

					attri.setComponent(component);
					context.addAccess(attri);
				}
			}
		}

		return context;
	}

	public AbsComponent componentInstance(ModuleDescription componentDes) {

		_logger.info("111loadComponent begin :" + componentDes.getInstanceName());
		AbsComponent component = new AbsComponent();
		component.setInstanceName(componentDes.getInstanceName());
		component.setURL(componentDes.getUrl());
		component.setRedisUtil(redisUtil);
		_logger.info("111loadComponent end :" + componentDes.getInstanceName());
		return component;

	}

	private AttributeAccess paserVarDes(VariableDescription varDes) {
		AttributeAccess attri = new AttributeAccess();
		String attributeName = "";
		String variableName = "";
		String variableValue = "";
		String variableType = "";
		String variableContext = "";
		String variableSize = "";
		if (varDes.getAttributeName() != null) {
			attributeName = varDes.getAttributeName();
			attri.setAttributeName(attributeName);
		}

		if (varDes.getVariableName() != null) {
			variableName = varDes.getVariableName();
			attri.setCompName(variableName);
		}

		if (varDes.getVariableValue() != null) {
			variableValue = varDes.getVariableValue();
			attri.setValue(variableValue);
		}
		if (varDes.getVariableType() != null) {
			variableType = varDes.getVariableType();
			attri.setDataType(variableType);
		}
		if (varDes.getVariableContext() != null && !"".equalsIgnoreCase(varDes.getVariableContext())) {
			variableContext = varDes.getVariableContext();
			Contextable poolContext = getContextList().get(variableContext);
			if (poolContext != null) {
				attri.setContext(poolContext);
			}
		}

		if (varDes.getVariableSize() != null && !"".equalsIgnoreCase(varDes.getVariableSize())) {
			variableSize = varDes.getVariableSize();
			attri.setSize(variableSize);
		}

		return attri;
	}

	private AttributeAccess paserVarDes(VariableDescription varDes, String compName) {
		AttributeAccess attri = new AttributeAccess();
		String attributeName = "";
		String variableName = "";
		String variableValue = "";
		String variableType = "";
		String variableContext = "";
		String variableSize = "";
		if (varDes.getAttributeName() != null) {
			attributeName = varDes.getAttributeName();
			attri.setAttributeName(attributeName);
		}

		if (varDes.getVariableName() != null) {
			variableName = varDes.getVariableName();
			attri.setCompName(variableName);
		}

		if (varDes.getVariableValue() != null) {
			variableValue = varDes.getVariableValue();
			attri.setValue(variableValue);
		}
		if (varDes.getVariableType() != null) {
			variableType = varDes.getVariableType();
			attri.setDataType(variableType);
		}
		if (varDes.getVariableContext() != null && !"".equalsIgnoreCase(varDes.getVariableContext())) {
			variableContext = varDes.getVariableContext();
			Contextable poolContext = getContextList().get(variableContext);
			if (poolContext != null) {
				attri.setContext(poolContext);
			}
		}
		if (varDes.getVariableSize() != null && !"".equalsIgnoreCase(varDes.getVariableSize())) {
			variableSize = varDes.getVariableSize();
			attri.setSize(variableSize);
		}
		// attri.setComponent(comp);
		return attri;
	}

	/**
	 * 加载java类，并将类实例化
	 *
	 * @param className
	 * @return 返回一个AbsComponent对象
	 */
	public AbsComponent loadComponent(String className) throws Exception {

		AbsComponent component = null;
		Class<?> clazz;

		_logger.info("111loadComponent begin :" + className);

		if (!"".equalsIgnoreCase(className) && className != null) {
			try {
				clazz = Class.forName(className);
			//	System.out.println("path=" + clazz.getResource("").getPath());

				this.getClass().getResource("").getPath();
				// clazz = hclassloader.loadClass(className);
			} catch (Exception e) {
				_logger.info(className + " is not exist");
				return null;
			}
			// generate an instance of that class
			if (clazz != null) {
				component = (AbsComponent) clazz.newInstance();
				component.setRedisUtil(redisUtil);

			}
		}
		_logger.info("111loadComponent end :" + className);
		return component;

	}

	/**
	 * @return the _contextList
	 */
	public Map<String, Contextable> getContextList() {
		return _contextList;
	}

	/**
	 * @param _contextList the _contextList to set
	 */
	public void setContextList(Map<String, Contextable> _contextList) {
		this._contextList = _contextList;
	}
}
