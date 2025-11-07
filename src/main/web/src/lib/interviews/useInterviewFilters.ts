import { computed, ref } from "vue";
import type { Ref } from "vue";
import type { Interview } from "../../types/Interview";
import type { TableFiltersState } from "../../types/TableFiltersState";
import {
	createQuestionTypeItems,
	DEFAULT_DATE_BOUNDS,
	DEFAULT_YOE_BOUNDS,
	filterInterviews,
} from "../../utils/interviews";

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
		yoeRange: [DEFAULT_YOE_BOUNDS.min, DEFAULT_YOE_BOUNDS.max],
		dateRange: { from: DEFAULT_DATE_BOUNDS.min, to: DEFAULT_DATE_BOUNDS.max },
	});

	const questionTypeItems = computed(() => createQuestionTypeItems());

	const filteredInterviews = computed(() => filterInterviews(interviews.value, filters.value));

	return {
		filters,
		filteredInterviews,
		questionTypeItems,
		metadata: {
			yoeBounds: { ...DEFAULT_YOE_BOUNDS },
			dateBounds: { ...DEFAULT_DATE_BOUNDS },
		},
	};
};
