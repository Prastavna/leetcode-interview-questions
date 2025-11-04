<script setup lang="ts">
import { computed, defineProps, ref, watch, withDefaults } from "vue";
import { getPaginationRowModel } from "@tanstack/vue-table";
import type { Interview, Round } from "../types/Interview";
import Rounds from "./Rounds.vue";
import {
	buildInterviewColumns,
	createPageSizeItems,
	interviewRowIdAccessor,
} from "../features/interviews/tableConfig";

const props = withDefaults(
  defineProps<{
    data?: Interview[];
    isLoading?: boolean;
    error?: string | null;
  }>(),
  {
    data: () => [],
    isLoading: false,
    error: null,
  },
);

const pagination = ref({
	pageIndex: 0,
	pageSize: 25,
});

const sorting = ref([
	{
		id: "date",
		desc: true,
	},
]);

const paginationOptions = {
	getPaginationRowModel: getPaginationRowModel(),
};

const pageSizeItems = createPageSizeItems();

const pageSizeModel = computed({
	get: () => pagination.value.pageSize,
	set: (value: number | string) => {
		const numeric = typeof value === "number" ? value : Number.parseInt(value, 10);
		if (Number.isNaN(numeric) || numeric <= 0) return;
		pagination.value = {
			...pagination.value,
			pageSize: numeric,
			pageIndex: 0,
		};
	},
});

const totalRows = computed(() => props.data?.length ?? 0);

const currentPage = computed({
	get: () => pagination.value.pageIndex + 1,
	set: (value: number | string) => {
		const numeric = typeof value === "number" ? value : Number.parseInt(value, 10);
		if (Number.isNaN(numeric) || numeric < 1) return;
		const pageSize = pagination.value.pageSize || 1;
		const maxPage = Math.max(1, Math.ceil(totalRows.value / pageSize || 1));
		const nextPage = Math.min(numeric, maxPage);
		pagination.value = {
			...pagination.value,
			pageIndex: nextPage - 1,
		};
	},
});

const pageSummary = computed(() => {
	const total = totalRows.value;
	if (total === 0) return "No records";
	const start = pagination.value.pageIndex * pagination.value.pageSize;
	const end = Math.min(start + pagination.value.pageSize, total);
	return `Showing ${start + 1}-${end} of ${total}`;
});

watch(
	[totalRows, () => pagination.value.pageSize],
	([total, pageSize]) => {
		const size = typeof pageSize === "number" ? pageSize : Number(pageSize) || 1;
		const maxPageIndex = total === 0 ? 0 : Math.max(0, Math.ceil(total / size) - 1);
		if (pagination.value.pageIndex > maxPageIndex) {
			pagination.value = {
				...pagination.value,
				pageIndex: maxPageIndex,
			};
		}
	},
	{ immediate: true },
);

const columns = buildInterviewColumns();

const expanded = ref<Record<string, boolean>>({});

watch(
	() => props.data,
	(rows?: Interview[]) => {
		const normalizedRows = Array.isArray(rows) ? rows : [];
		const nextExpanded: Record<string, boolean> = {};
		const validIds = new Set(
			normalizedRows.map((row, index) => interviewRowIdAccessor(row, index)),
		);
		let didChange = false;
		for (const [key, value] of Object.entries(expanded.value)) {
			if (validIds.has(key) && value) {
				nextExpanded[key] = value;
			} else if (value) {
				didChange = true;
			}
		}
		if (didChange) {
			expanded.value = nextExpanded;
		}
	},
	{ immediate: true },
);
</script>

<template>
  <div class="flex flex-col flex-1 w-full">
    <div class="p-4" v-if="props.isLoading">Loading interviews…</div>
    <div class="p-4 text-red-600" v-else-if="props.error">{{ props.error }}</div>
    <div v-else>
      <div class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
        <span class="text-sm text-gray-500">
          {{ pageSummary }}
        </span>

        <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
          <div class="flex items-center gap-2">
            <span class="text-xs font-medium uppercase tracking-wide text-gray-500">Rows</span>
            <USelect v-model="pageSizeModel" :items="pageSizeItems" class="w-28" />
          </div>
          <UPagination v-model:page="currentPage" :items-per-page="pagination.pageSize" :total="totalRows" />
        </div>
      </div>

      <UTable
        v-model:expanded="expanded"
        v-model:pagination="pagination"
        v-model:sorting="sorting"
        ref="table"
        :data="props.data"
        :columns="columns"
        :pagination-options="paginationOptions"
        :get-row-id="interviewRowIdAccessor"
      >
        <template #expanded="{ row }">
          <Rounds :rounds="(row.original?.rounds ?? []) as Round[]" />
        </template>
      </UTable>
    </div>

  </div>
</template>
