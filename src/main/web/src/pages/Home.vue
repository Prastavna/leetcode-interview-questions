<template>
  <div class="flex flex-col gap-6">
    <TableFilters
      v-model="filters"
      :question-type-options="questionTypeItems"
      :min-yoe="YOE_MIN"
      :max-yoe="YOE_MAX"
      :min-date="dateBounds.min"
      :max-date="dateBounds.max"
    />

    <Table :data="filteredInterviews" :is-loading="isLoading" :error="error" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import Table from "../components/Table.vue";
import TableFilters, { type TableFiltersState } from "../components/TableFilters.vue";
import type { Interview } from "../types/Interview";
import { QuestionType } from "../types/Interview";

const interviews = ref<Interview[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const YOE_MIN = 0;
const YOE_MAX = 35;

const filters = ref<TableFiltersState>({
	search: "",
	questionTypes: [],
	yoeRange: [YOE_MIN, YOE_MAX],
	dateRange: { from: null, to: null },
});

const questionTypeItems = computed(() =>
	Object.entries(QuestionType).map(([value, label]) => ({
		value,
		label,
	}))
);

const toDateInputString = (value: Date | string | null | undefined) => {
	if (!value) return null;
	const date = value instanceof Date ? value : new Date(value);
	if (Number.isNaN(date.getTime())) return null;
	const offset = date.getTimezoneOffset();
	const local = new Date(date.getTime() - offset * 60000);
	return local.toISOString().slice(0, 10);
};

const dateBounds = computed(() => {
	if (!interviews.value.length) {
		return { min: null as string | null, max: null as string | null };
	}
	let minDate: Date | null = null;
	let maxDate: Date | null = null;
	for (const interview of interviews.value) {
		if (!interview.date) continue;
		const current = new Date(interview.date);
		if (Number.isNaN(current.getTime())) continue;
		if (!minDate || current < minDate) {
			minDate = current;
		}
		if (!maxDate || current > maxDate) {
			maxDate = current;
		}
	}
	return {
		min: minDate ? toDateInputString(minDate) : null,
		max: maxDate ? toDateInputString(maxDate) : null,
	};
});

watch(
	dateBounds,
	(bounds) => {
		const nextFrom = filters.value.dateRange.from ?? bounds.min;
		const nextTo = filters.value.dateRange.to ?? bounds.max;
		if (filters.value.dateRange.from !== nextFrom || filters.value.dateRange.to !== nextTo) {
			filters.value = {
				...filters.value,
				dateRange: {
					from: nextFrom,
					to: nextTo,
				},
			};
		}
	},
	{ immediate: true },
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

onMounted(async () => {
	try {
		const res = await fetch("/interviews.json", { cache: "no-store" });
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
