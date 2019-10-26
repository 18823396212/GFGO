package ext.appo.part.beans;

import java.io.Serializable;
import java.util.Locale;

import wt.part.WTPart;

public class PartInfoBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private String number;

	private String name;

	private String viewName;

	private String version;

	private String iteration;

	private String lifecycle;

	private boolean success;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIteration() {
		return iteration;
	}

	public void setIteration(String iteration) {
		this.iteration = iteration;
	}

	public String getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PartInfoBean [number=");
		builder.append(number);
		builder.append(", name=");
		builder.append(name);
		builder.append(", viewName=");
		builder.append(viewName);
		builder.append(", version=");
		builder.append(version);
		builder.append(", iteration=");
		builder.append(iteration);
		builder.append(", lifecycle=");
		builder.append(lifecycle);
		builder.append(", success=");
		builder.append(success);
		builder.append("]");
		return builder.toString();
	}

	public static PartInfoBean createBean(WTPart wtpart) {
		PartInfoBean bean = new PartInfoBean();

		if (wtpart != null) {
			bean.setNumber(wtpart.getNumber());
			bean.setName(wtpart.getName());
			bean.setViewName(wtpart.getViewName());
			bean.setVersion(wtpart.getVersionIdentifier().getValue());
			bean.setIteration(wtpart.getIterationIdentifier().getValue());
			bean.setLifecycle(wtpart.getLifeCycleState().getDisplay(Locale.CHINA));
		}

		return bean;
	}
}
