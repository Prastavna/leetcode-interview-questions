<template>
  <div class="flex flex-col gap-6 w-5/6 mx-auto">
    <div v-if="!isLoading" class="grid gap-6 lg:grid-cols-2">
      <QuestionTypeDonutChart :interviews="filteredInterviews" />
      <CompanyQuestionTypeStackedBarChart :interviews="filteredInterviews" />
    </div>

    <TableFilters
      v-model="filters"
      :question-type-options="questionTypeItems"
      :min-yoe="YOE_MIN"
      :max-yoe="YOE_MAX"
      :min-date="MIN_DATE"
      :max-date="MAX_DATE"
    />

    <Table :data="filteredInterviews" :is-loading="isLoading" :error="error" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import Table from "../components/Table.vue";
import TableFilters, { type TableFiltersState } from "../components/TableFilters.vue";
import QuestionTypeDonutChart from "../components/QuestionTypeDonutChart.vue";
import CompanyQuestionTypeStackedBarChart from "../components/CompanyQuestionTypeStackedBarChart.vue";
import type { Interview } from "../types/Interview";
import { QuestionType } from "../types/Interview";

const interviews = ref<Interview[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const YOE_MIN = 0;
const YOE_MAX = 35;

const toDateInputString = (value: Date | string | null | undefined) => {
	if (!value) return null;
	const date = value instanceof Date ? value : new Date(value);
	if (Number.isNaN(date.getTime())) return null;
	const offset = date.getTimezoneOffset();
	const local = new Date(date.getTime() - offset * 60000);
	return local.toISOString().slice(0, 10);
};

const MIN_DATE = "2025-01-01";
const MAX_DATE = toDateInputString(new Date()) ?? MIN_DATE;

const filters = ref<TableFiltersState>({
	search: "",
	questionTypes: [],
	yoeRange: [YOE_MIN, YOE_MAX],
	dateRange: { from: MIN_DATE, to: MAX_DATE },
});

const questionTypeItems = computed(() =>
	Object.entries(QuestionType).map(([value, label]) => ({
		value: value as string,
		label: String(label),
	})) as { value: string; label: string }[]
);

const filteredInterviews = computed(() => {
	if (!interviews.value.length) return [] as Interview[];
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
				round?.questions?.some((question) => question?.type && selectedTypes.has(question.type))
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


const datasetUrl = (() => {
	const base = import.meta.env.BASE_URL ?? "/";
	return base.endsWith("/") ? `${base}interviews.json` : `${base}/interviews.json`;
})();

onMounted(async () => {
	try {
		const res = await fetch(datasetUrl, { cache: "no-store" });
		if (!res.ok) throw new Error(`Failed to load interviews.json (${res.status})`);
		const json = await res.json();
		interviews.value = Array.isArray(json) ? (json as Interview[]) : [];
	} catch (e: any) {
		error.value = e?.message ?? "Unknown error loading data";
	} finally {
		isLoading.value = false;
	}
});
</script>
