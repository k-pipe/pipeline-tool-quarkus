package pipelining.script.model;

public enum StepType {
	SCRIPT,
	JOB,
	PREDEFINED,
	DELAY, // wait some predefined time
	INPUT, // constant input or wait until some user provides input
	SUCCEED,
	FAILED
}
