import type { Interview } from "../types/Interview";
import { QuestionType } from "../types/Interview";
import type { TableFiltersState } from "../types/TableFiltersState";

export const INTERVIEW_DATASET_FILENAME = "interviews.json";

export const buildInterviewsDatasetUrl = (baseUrl?: string) => {
	const base = typeof baseUrl === "string" && baseUrl.length > 0 ? baseUrl : "/";
	const normalized = base.endsWith("/") ? base : `${base}/`;
	return `${normalized}${INTERVIEW_DATASET_FILENAME}`;
};

export const toDateInputString = (value: Date | string | null | undefined) => {
	if (!value) return null;
	const date = value instanceof Date ? value : new Date(value);
	if (Number.isNaN(date.getTime())) return null;
	const offset = date.getTimezoneOffset();
	const local = new Date(date.getTime() - offset * 60000);
	return local.toISOString().slice(0, 10);
};

export const DEFAULT_YOE_BOUNDS = { min: 0, max: 35 } as const;
export const MIN_INTERVIEW_DATE = "2025-01-01";
export const DEFAULT_DATE_BOUNDS = {
	min: MIN_INTERVIEW_DATE,
	max: toDateInputString(new Date()) ?? MIN_INTERVIEW_DATE,
} as const;

export type QuestionTypeItem = {
	value: string;
	label: string;
};

export const createQuestionTypeItems = (): QuestionTypeItem[] =>
	Object.entries(QuestionType).map(([value, label]) => ({
		value,
		label: String(label),
	}));

const normalizeInterviewDate = (value: Interview["date"]) => {
	if (!value) return null;
	const date = new Date(value);
	return Number.isNaN(date.getTime()) ? null : date;
};

export const filterInterviews = (interviews: Interview[], filters: TableFiltersState) => {
	if (!Array.isArray(interviews) || interviews.length === 0) {
		return [];
	}

	const searchTerm = filters.search.trim().toLowerCase();
	const selectedTypes = new Set(filters.questionTypes);
	const [minYoe, maxYoe] = filters.yoeRange;
	const fromDate = filters.dateRange.from ? new Date(filters.dateRange.from) : null;
	const toDate = filters.dateRange.to ? new Date(filters.dateRange.to) : null;

	return interviews.filter((interview) => {
		if (searchTerm) {
			const company = interview.company?.toLowerCase() ?? "";
			const role = interview.role?.toLowerCase() ?? "";
			if (!company.includes(searchTerm) && !role.includes(searchTerm)) {
				return false;
			}
		}

		if (selectedTypes.size) {
			const matchesType = interview.rounds?.some((round) =>
				round?.questions?.some((question) => question?.type && selectedTypes.has(question.type)),
			);
			if (!matchesType) return false;
		}

		const yoe = typeof interview.yoe === "number" ? interview.yoe : Number(interview.yoe);
		if (Number.isFinite(minYoe) && yoe < minYoe) return false;
		if (Number.isFinite(maxYoe) && yoe > maxYoe) return false;

		if (fromDate || toDate) {
			const interviewDate = normalizeInterviewDate(interview.date);
			if (!interviewDate) return false;
			if (fromDate && interviewDate < fromDate) return false;
			if (toDate && interviewDate > toDate) return false;
		}

		return true;
	});
};
