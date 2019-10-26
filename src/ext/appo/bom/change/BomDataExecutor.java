package ext.appo.bom.change;

import java.util.Vector;

import com.ptc.core.meta.common.DataTypesUtility;
import com.ptc.core.meta.common.FloatingPoint;

import ext.appo.util.PartUtil;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.occurrence.OccurrenceHelper;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTPropertyVetoException;

public class BomDataExecutor {
	private static String occurrenceold;
	private static String occurrencenew;

	public BomDataExecutor() {

	}

	public static String filterOccurrence(String occurrenceold, ChangeBOMObject object) {
		String occurencenewString = "";
		String[] ss = occurrenceold.split(",");
		// 把数组装载到一个vector里面
		Vector vector = new Vector();
		for (int i = 0; i < ss.length; i++) {
			vector.add(ss[i]);
		}
		for (int k = 0; k < vector.size(); k++) {
			if (object.getOccurrence().equals(vector.get(k).toString())) {
				vector.remove(object.getOccurrence());
			}
		}
		for (int j = 0; j < vector.size(); j++) {
			if (occurencenewString.length() > 0) {
				occurencenewString = occurencenewString + ",";
			}
			occurencenewString = occurencenewString + vector.get(j).toString();
		}

		return occurencenewString;
	}

	// 该方法用于验证BOM中是否有重复的位号，有，返回错误信息
	public static Vector checkOccurrence(WTPart parentPart, ChangeBOMObject object) {
		Vector messageVector = new Vector();
		Vector OccurrenceVector = new Vector();
		QueryResult qr = new QueryResult();
		if (object.getOccurrencmodify().equals("增加")) {
			try {
				qr = WTPartHelper.service.getUsesWTPartMasters(parentPart);
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 获取所有的位号
			while (qr.hasMoreElements()) {
				WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
				try {
					String occurrence = CheckUtil.getReferenceDesignators(link);
					String[] ss = occurrence.split(",");
					for (int i = 0; i < ss.length; i++) {
						OccurrenceVector.add(ss[i]);
					}
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (OccurrenceVector.size() > 0) {

				String occurrence1 = object.getOccurrence();
				String[] ss = occurrence1.split(",");
				for (int k = 0; k < ss.length; k++) {
					if (OccurrenceVector.contains(ss[k])) {
						messageVector.add("第" + object.getId() + "行 位号" + ss[k] + "与原位号重复！！");

					}
				}
			}
		}
		return messageVector;
	}

	protected Vector<String> addBomLine(ChangeBOMObject changeBOMObject) {
		System.out.println("addBomLine==>START " + changeBOMObject);

		Vector<String> vector = new Vector<String>();

		WTPart parentPart = null;
		parentPart = PartUtil.getLastestWTPartByNumber(changeBOMObject.getParentnumber());

		WTPartMaster master = null;
		try {
			master = getWTPartMasterByNumber(changeBOMObject.getChildnumber());
		} catch (WTException e1) {
			e1.printStackTrace();
		}

		if (parentPart == null) {
			vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber() + "没有在系统中查询到");
		}

		if (master == null) {
			vector.add("第" + changeBOMObject.getId() + "行 子层物料编号" + changeBOMObject.getChildnumber() + "没有在系统中查询到");
		}
		if (parentPart != null && master != null) {
			QueryResult qr = new QueryResult();
			try {
				parentPart = (WTPart) BOMChangeUtil.checkoutWTPart(parentPart, "");
				qr = WTPartHelper.service.getUsesWTPartMasters(parentPart); // ��׼WCҳ����ʹ�õ�API
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
			System.out.println(" queryResult=" + qr.size());
			WTPartUsageLink usagelink = null;

			try {
				QuantityUnit quantityUnit = QuantityUnit.toQuantityUnit(changeBOMObject.getUnit());
				Quantity quantity = Quantity.newQuantity(Double.parseDouble(changeBOMObject.getQuantity()),
						quantityUnit);

				usagelink = WTPartUsageLink.newWTPartUsageLink(parentPart, master);
				usagelink.setQuantity(quantity);
				usagelink = (WTPartUsageLink) PersistenceHelper.manager.save(usagelink);
			} catch (WTInvalidParameterException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
			if (usagelink == null) {
				vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber() + ",子件物料编号"
						+ changeBOMObject.getChildnumber() + "没能成功创建BOM引用关系!");
			}

			// 现在开始更新位号 》》》》》》直接在创建时添加位号不成功
			if (changeBOMObject.getOccurrence().length() > 0) {
				try {
					qr = WTPartHelper.service.getUsesWTPartMasters(parentPart);// 获取所有的link关系
					System.out.println("qr.size()===" + qr.size());
				} catch (WTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (qr.hasMoreElements()) {
					WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
					WTPartMaster partmasterObj = (WTPartMaster) link.getRoleBObject();
					if (partmasterObj.getNumber().equalsIgnoreCase(changeBOMObject.getChildnumber())) {
						System.out.println("partmasterObj.getNumber()====" + partmasterObj.getNumber());
						System.out.println("changeBOMObject.getChildnumber()====" + changeBOMObject.getChildnumber());
						try {
							String quantityStr = getBomQuantity(link);
							System.out.println(
									"quantityStr=" + quantityStr + ", oldQty=" + changeBOMObject.getOldquantity());
							String occurrence = CheckUtil.getReferenceDesignators(link);
							System.out.println("occurrence===" + occurrence);
							if ((changeBOMObject.getOccurrence() != null)
									&& (changeBOMObject.getOccurrence().length() > 0)) {
								PartUsesOccurrence newOccurrence;
								try {
									newOccurrence = PartUsesOccurrence.newPartUsesOccurrence(link);
									newOccurrence.setName(changeBOMObject.getOccurrence());
									OccurrenceHelper.service.saveUsesOccurrenceAndData(newOccurrence, null);
								} catch (WTPropertyVetoException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							PersistenceHelper.manager.save(link);
							parentPart = (WTPart) BOMChangeUtil.checkinWTPart(parentPart, "checkin");
							vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber()
									+ "子物料编号" + changeBOMObject.getChildnumber() + "创建成功");

						} catch (WTException e) {
							e.printStackTrace();
						}
					}
				}

			}
		}
		System.out.println("2+++++++++++++++++++++++++++++++addBOmLine" + changeBOMObject.getParentnumber() + "  "
				+ changeBOMObject.getChildnumber());
		try {
			parentPart = (WTPart) BOMChangeUtil.checkinWTPart(parentPart, "checkin");
			vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber() + "子物料编号"
					+ changeBOMObject.getChildnumber() + "创建成功");
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("addBomLine==>END " + changeBOMObject);

		return vector;
	}

	protected WTPartMaster getWTPartMasterByNumber(String number) throws WTException {
		WTPartMaster master = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(WTPartMaster.class);
		SearchCondition sc = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL,
				number.toUpperCase());
		qs.appendSearchCondition(sc);
		qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			master = (WTPartMaster) qr.nextElement();
			break;
		}
		return master;
	}

	public static long getMaxLineNumber(long[] array) {
		Long big = Long.valueOf(0);
		for (int i = 0; i < array.length; i++) {
			if (big < array[i]) {
				big = array[i];
			}
		}
		return big + 10;
	}

	/**
	 * @param bomEdmObject
	 *            ������� ����OK
	 * @throws WTPropertyVetoException
	 */
	protected Vector<String> updateBomLine(ChangeBOMObject changeBOMObject) {
		System.out.println("updateBomLine==>START " + changeBOMObject);

		Vector<String> vector = new Vector();

		WTPart parentPart = null;
		parentPart = PartUtil.getLastestWTPartByNumber(changeBOMObject.getParentnumber());

		if (parentPart == null) {
			vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber() + "没有在系统中查询到");
		} else {
			QueryResult qr = new QueryResult();

			try {
				parentPart = (WTPart) BOMChangeUtil.checkoutWTPart(parentPart, "checkout");

			} catch (WTException e1) {
				e1.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
			/*
			 * 增加对位号操作的判断逻辑 1,如果是增加位号的操作，可直接执行update操作
			 * 2,如果是删除操作，先删除原来的link，保留原来的位号字段，进行修改得到新的位号字段，
			 * 然后创建一个位号为空的link进行update操作！
			 */
			// 若需要修改位号，或者原来位号不为空，这里先做删除操作
			if (!changeBOMObject.getOccurrencmodify().equals("增加"))// 不是增加操作
			{
				if (changeBOMObject.getOccurrence() != null || changeBOMObject.getOccurrence().length() > 0) {
					try {
						qr = WTPartHelper.service.getUsesWTPartMasters(parentPart);// 获取所有的link关系
					} catch (WTException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					while (qr.hasMoreElements()) {
						WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
						WTPartMaster partmasterObj = (WTPartMaster) link.getRoleBObject();

						if (partmasterObj.getNumber().equalsIgnoreCase(changeBOMObject.getChildnumber())) {

							try {
								String occurrence = CheckUtil.getReferenceDesignators(link);
								if (occurrence != null || occurrence.length() > 0)// 原位号不为空，侧必须进行删除操作
								{
									this.occurrenceold = occurrence;
									PersistenceHelper.manager.delete(link);
								}
							} catch (WTException e) {
								e.printStackTrace();
							}
						}

					}
				}
				// 删除后创建一条新看link，用于更新位号
				WTPartUsageLink usagelink = null;
				WTPartMaster master = null;
				try {
					master = getWTPartMasterByNumber(changeBOMObject.getChildnumber());
				} catch (WTException e1) {
					e1.printStackTrace();
				}
				try {
					QuantityUnit quantityUnit = QuantityUnit.toQuantityUnit("ea");
					Quantity quantity = Quantity.newQuantity(Double.parseDouble(changeBOMObject.getQuantity()),
							quantityUnit);

					usagelink = WTPartUsageLink.newWTPartUsageLink(parentPart, master);
					usagelink.setQuantity(quantity);
					usagelink = (WTPartUsageLink) PersistenceHelper.manager.save(usagelink);
				} catch (WTInvalidParameterException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (WTException e) {
					e.printStackTrace();
				}
				this.occurrencenew = filterOccurrence(this.occurrenceold, changeBOMObject);// 对原位号进行删除操作
			} else {

				this.occurrencenew = changeBOMObject.getOccurrence();
			}

			System.out.println("this.occurrencenew======" + this.occurrencenew);
			// 现在开始更新位号
			try {
				qr = WTPartHelper.service.getUsesWTPartMasters(parentPart);// 获取所有的link关系
			} catch (WTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (qr.hasMoreElements()) {
				WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
				WTPartMaster partmasterObj = (WTPartMaster) link.getRoleBObject();

				if (partmasterObj.getNumber().equalsIgnoreCase(changeBOMObject.getChildnumber())) {
					try {
						String quantityStr = getBomQuantity(link);
						System.out
								.println("quantityStr=" + quantityStr + ", oldQty=" + changeBOMObject.getOldquantity());
						String occurrence = CheckUtil.getReferenceDesignators(link);
						System.out.println("occurrence===" + occurrence);

						// if
						// (!quantityStr.equalsIgnoreCase(changeBOMObject.getOldquantity()))
						// {
						System.out.println("found the quantity");
						QuantityUnit quantityUnit = null;
						if (changeBOMObject.getUnit().length() > 0) {
							quantityUnit = QuantityUnit.toQuantityUnit(changeBOMObject.getUnit());
						} else {
							quantityUnit = QuantityUnit.toQuantityUnit("ea");
						}
						Quantity quantity = Quantity.newQuantity(Double.parseDouble(changeBOMObject.getQuantity()),
								quantityUnit);

						link.setQuantity(quantity);
						if ((changeBOMObject.getOccurrence() != null)
								&& (changeBOMObject.getOccurrence().length() > 0)) {
							PartUsesOccurrence newOccurrence;
							try {
								newOccurrence = PartUsesOccurrence.newPartUsesOccurrence(link);
								newOccurrence.setName(this.occurrencenew);
								OccurrenceHelper.service.saveUsesOccurrenceAndData(newOccurrence, null);
							} catch (WTPropertyVetoException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						PersistenceHelper.manager.save(link);
						vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber()
								+ "子物料编号" + changeBOMObject.getChildnumber() + "更新成功");

						// }

					} catch (WTException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				parentPart = (WTPart) BOMChangeUtil.checkinWTPart(parentPart, "");
			} catch (WTException e) {
				e.printStackTrace();
			}

		}
		System.out.println("updateBomLine==>START " + changeBOMObject);

		return vector;
	}

	public String getBomQuantity(WTPartUsageLink usagelink) throws WTException {
		String quantity = "";
		FloatingPoint floatingpoint = new FloatingPoint(usagelink.getQuantity().getAmount(), -1);
		String s3 = DataTypesUtility.toString(floatingpoint, SessionHelper.getLocale());
		if (s3 != null && s3.trim().length() > 0)
			quantity = s3;
		else
			quantity = "1";
		return quantity;
	}

	protected Vector<String> deleteBomLine(ChangeBOMObject changeBOMObject) {
		System.out.println("deleteBomLine==>START " + changeBOMObject);
		Vector<String> vector = new Vector();

		WTPart parentPart = null;
		parentPart = PartUtil.getLastestWTPartByNumber(changeBOMObject.getParentnumber());

		if (parentPart == null) {
			vector.add("第" + changeBOMObject.getId() + "行 父层物料编号" + changeBOMObject.getParentnumber() + "没有在系统中查询到");
		} else {
			QueryResult qr = new QueryResult();

			try {
				parentPart = (WTPart) BOMChangeUtil.checkoutWTPart(parentPart, "");
			} catch (WTPropertyVetoException e2) {
				e2.printStackTrace();
			} catch (WTException e2) {
				e2.printStackTrace();
			}

			try {
				qr = WTPartHelper.service.getUsesWTPartMasters(parentPart);
			} catch (WTException e1) {
				e1.printStackTrace();
			}

			while (qr.hasMoreElements()) {
				WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
				WTPartMaster partmasterObj = (WTPartMaster) link.getRoleBObject();

				if (partmasterObj.getNumber().equalsIgnoreCase(changeBOMObject.getChildnumber())) {

					System.out.println("found the sub Part " + partmasterObj.getNumber());
					try {
						PersistenceHelper.manager.delete(link);
						vector.add("第" + changeBOMObject.getId() + "行物料编号" + changeBOMObject.getChildnumber() + "删除成功");
					} catch (WTException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				parentPart = (WTPart) BOMChangeUtil.checkinWTPart(parentPart, "");
			} catch (WTException e) {
				e.printStackTrace();
			}

		}

		System.out.println("deleteBomLine==>END " + changeBOMObject);

		return vector;
	}
}
