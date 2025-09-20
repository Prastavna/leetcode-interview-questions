export const QuestionType = {
	APTITUDE: "Aptitude",
	DEBUGGING: "Debugging",
  CULTURE_FIT: "Culture Fit",
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
	TAKE_HOME_ASSIGNMENT: "Take Home Assignment",
	TECHNICAL: "Technical",
  SCREENING: "Screening",
	SYSTEM_DESIGN: "System Design",
} as const;

export const QuestionTypeColors: Record<keyof typeof QuestionType, string> = {
	TECHNICAL: "#00c16a",
	DSA: "#5ec985",
	SYSTEM_DESIGN: "#8bcfa0",
	MACHINE_CODING: "#b1d5bb",
	LLD: "#87baab",
	HLD: "#8ccbba",
	PROJECT_DISCUSSION: "#91dcc9",
	APTITUDE: "#fc7a57",
	DEBUGGING: "#ff9b7e",
	CULTURE_FIT: "#ffbba6",
	GD: "#ffdacf",
	HM: "#95edd9",
	HR: "#98ffea",
	LANGUAGE_SPECIFIC: "#a491d3",
	OTHER: "#d0bdea",
	TAKE_HOME_ASSIGNMENT: "#c2addf",
	SCREENING: "#b49dd4",
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
