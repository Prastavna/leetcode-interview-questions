import { computed, ref } from "vue";
import type { Ref } from "vue";
import type { Interview } from "../../../types/Interview";
import { QuestionType } from "../../../types/Interview";
import type { TableFiltersState } from "../types";
import { toDateInputString } from "../utils";

const YOE_MIN = 0;
const YOE_MAX = 35;
const MIN_DATE = "2025-01-01";
const MAX_DATE = toDateInputString(new Date()) ?? MIN_DATE;

export type InterviewFilterMetadata = {
	yoeBounds: { min: number; max: number };
	dateBounds: { min: string | null; max: string | null };
};

export const useInterviewFilters = (interviews: Ref<Interview[]>): {
	filters: Ref<TableFiltersState>;
	filteredInterviews: Ref<Interview[]>;
	questionTypeItems: Ref<{ value: string; label: string }[]>;
	metadata: InterviewFilterMetadata;
} => {
	const filters = ref<TableFiltersState>({
		search: "",
		questionTypes: [],
		yoeRange: [YOE_MIN, YOE_MAX],
		dateRange: { from: MIN_DATE, to: MAX_DATE },
	});

	const questionTypeItems = computed(() =>
		Object.entries(QuestionType).map(([value, label]) => ({
			value,
			label: String(label),
		})),
	);

	const filteredInterviews = computed(() => {
		if (!interviews.value.length) return [];

		const searchTerm = filters.value.search.trim().toLowerCase();
		const selectedTypes = new Set(filters.value.questionTypes);
		const [minYoe, maxYoe] = filters.value.yoeRange;
		const fromDate = filters.value.dateRange.from ? new Date(filters.value.dateRange.from) : null;
		const toDate = filters.value.dateRange.to ? new Date(filters.value.dateRange.to) : null;

		return interviews.value.filter((interview) => {
			if (searchTerm) {
				const company = interview.company?.toLowerCase() ?? "";
				const role = interview.role?.toLowerCase() ?? "";
				if (!company.includes(searchTerm) && !role.includes(searchTerm)) return false;
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
				if (!interview.date) return false;
				const interviewDate = new Date(interview.date);
				if (Number.isNaN(interviewDate.getTime())) return false;
				if (fromDate && interviewDate < fromDate) return false;
				if (toDate && interviewDate > toDate) return false;
			}

			return true;
		});
	});

	return {
		filters,
		filteredInterviews,
		questionTypeItems,
		metadata: {
			yoeBounds: { min: YOE_MIN, max: YOE_MAX },
			dateBounds: { min: MIN_DATE, max: MAX_DATE },
		},
	};
};
