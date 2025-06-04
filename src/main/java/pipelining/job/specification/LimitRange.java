package pipelining.job.specification;

public class LimitRange {
	public int minimum;
	public int maximum;
	public int increment;
	public float factor;

	public LimitRange() {
	}

	public LimitRange(final int minimum, final int maximum, final int increment, final float factor) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.increment = increment;
		this.factor = factor;
	}

	public LimitRange(final int value) {
		this(value, value, 0, 1.0f);
	}

	public int getMinimum() {
		return minimum;
	}

	public int getMaximum() {
		return maximum;
	}

	public int getIncrement() {
		return increment;
	}

	public float getFactor() {
		return factor;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}
}
