package ext.appo.part.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ptc.core.components.beans.TreeHandlerAdapter;
import com.ptc.netmarkets.model.NmSimpleOid;

import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.MversionControlHelper;
import wt.util.WTException;

public class EffecitveBaselineHandler extends TreeHandlerAdapter {

	private List<EffectiveBaselineBean> beans;

	public EffecitveBaselineHandler(List<EffectiveBaselineBean> beans) {
		this.beans = beans;
	}

	@Override
	public Map<Object, List> getNodes(List list) throws WTException {
		Map<Object, List> map = new HashMap<Object, List>();
		System.out.println("list===============================================" + list);
		for (Object obj : list) {
			System.out.println("obj====================" + obj);
			if (obj instanceof NmSimpleOid) {
				NmSimpleOid nso = (NmSimpleOid) obj;
				System.out.println("nso1==========================" + nso);
				List l = MversionControlHelper.getEffBaselineNode(beans, nso);
				System.out.println("l1===================================================" + l);
				if (l != null && l.size() > 0)
					map.put(obj, l);
			}
			if (obj instanceof String) {
				NmSimpleOid nso = new NmSimpleOid();
				System.out.println("nso2====================================" + nso);
				nso.setInternalName(obj.toString());
				List l = MversionControlHelper.getEffBaselineNode(beans, nso);
				System.out.println("l2===================================================" + l);
				if (l != null && l.size() > 0)
					map.put(obj, l);
			}
		}
		return map;
	}

	@Override
	public List<Object> getRootNodes() throws WTException {
		List<Object> list = new ArrayList<>();
		System.out.println("beanas=====================" + beans);
		for (EffectiveBaselineBean bean : beans) {
			if ("1".equals(bean.getLevel())) {
				NmSimpleOid simpleOid = new NmSimpleOid();
				simpleOid.setInternalName(bean.getoid());
				simpleOid.setRef(simpleOid.getInternalName());
				simpleOid.setDisplayIdentifier(simpleOid.getInternalName());
				if (!list.contains(simpleOid))
					list.add(simpleOid);
			}
		}
		System.out.println("list3===================================================" + list);
		if (list.size() > 0)
			return list;
		else
			return null;
	}
}
