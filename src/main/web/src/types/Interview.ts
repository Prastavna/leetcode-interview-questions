export const QuestionType = {
	APTITUDE: "Aptitude",
  BEHAVIOURAL: "Behavioural",
  CULTURE_FIT: "Culture Fit",
  DEBUGGING: "Debugging",
	DSA: "DSA",
	GD: "Group Discussion",
	HLD: "High Level Design",
	HM: "Hiring Manager",
	HR: "HR",
	LANGUAGE_SPECIFIC: "Language Specific",
	LLD: "Low Level Design",
	MACHINE_CODING: "Machine Coding",
  OTHER: "Other",
	PROJECT_DISCUSSION: "Project Discussion",
  SCREENING: "Screening",
	SYSTEM_DESIGN: "System Design",
  TAKE_HOME_ASSIGNMENT: "Take Home Assignment",
  TECHNICAL: "Technical",
} as const;

export const QuestionTypeColors: Record<keyof typeof QuestionType, string> = {
  APTITUDE: "#fc7a57",
  BEHAVIOURAL: "#ea9ecf",
  CULTURE_FIT: "#ffbba6",
  DEBUGGING: "#ff9b7e",
	DSA: "#5ec985",
  GD: "#ffdacf",
  HLD: "#8ccbba",
  HM: "#95edd9",
  HR: "#98ffea",
  LANGUAGE_SPECIFIC: "#a491d3",
  LLD: "#87baab",
	MACHINE_CODING: "#b1d5bb",
	OTHER: "#d0bdea",
  PROJECT_DISCUSSION: "#91dcc9",
  SCREENING: "#b49dd4",
  SYSTEM_DESIGN: "#8bcfa0",
	TAKE_HOME_ASSIGNMENT: "#c2addf",
  TECHNICAL: "#00c16a",
};

export type Question = {
	id: string;
	type: keyof typeof QuestionType;
	content: string;
};

export type Round = {
	id: string;
	questions: Question[];
};

export type Interview = {
	id: string;
	leetcodeId: string;
	company: string;
	role: string;
	yoe: number;
	rounds: Round[];
	date: string;
};
