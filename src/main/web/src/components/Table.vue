<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { getPaginationRowModel, type Row } from "@tanstack/vue-table";
import type { TableColumn } from "@nuxt/ui";
import type { Interview, Round } from "../types/Interview";
import Rounds from "./Rounds.vue";
import RoundChip from "./RoundChip.vue";
import SortableHeader from "./SortableHeader.vue";

const PAGE_SIZE_OPTIONS = [10, 25, 50, 100, 10000] as const;
type PageSizeOption = (typeof PAGE_SIZE_OPTIONS)[number];

const withColumnWidth = (width: string) => ({
	style: {
		th: { width },
		td: { width },
	},
});

const createPageSizeItems = () =>
	PAGE_SIZE_OPTIONS.map((value) => ({
		label: value === 10000 ? "All" : String(value),
		value,
	}));

const columns: TableColumn<Interview>[] = [
	{
		id: "expand",
		enableSorting: false,
		meta: withColumnWidth("4%"),
	},
	{
		id: "leetcodeId",
		accessorKey: "leetcodeId",
		header: "#",
		enableSorting: false,
		meta: withColumnWidth("6%"),
	},
	{
		id: "company",
		accessorKey: "company",
		header: "Company",
		enableSorting: true,
		meta: withColumnWidth("14%"),
	},
	{
		id: "role",
		accessorKey: "role",
		header: "Role",
		enableSorting: true,
		meta: withColumnWidth("14%"),
	},
	{
		id: "yoe",
		accessorKey: "yoe",
		header: "YoE",
		enableSorting: true,
		sortingFn: "basic",
		meta: withColumnWidth("6%"),
	},
	{
		id: "roundCount",
		accessorFn: (row) => (Array.isArray(row.rounds) ? row.rounds.length : 0),
		header: "# of Rounds",
		enableSorting: true,
		sortingFn: "basic",
		meta: withColumnWidth("10%"),
	},
	{
		id: "roundDetails",
		header: "Round Types",
		meta: withColumnWidth("40%"),
	},
	{
		id: "date",
		accessorFn: (row: Interview) => {
			const timestamp = Date.parse(String(row.date));
			return Number.isNaN(timestamp) ? 0 : timestamp;
		},
		header: "Date",
		enableSorting: true,
		sortingFn: "datetime",
		meta: withColumnWidth("6%"),
	},
];

const interviewRowIdAccessor = (row: Interview, index: number) => {
	const identifier = row.id || row.leetcodeId;
	if (identifier) {
		return identifier;
	}
	return `${row.company ?? "company"}-${row.role ?? "role"}-${row.date ?? index}-${index}`;
};

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

const pagination = ref<{
  pageIndex: number;
  pageSize: PageSizeOption
}>({
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
	set: (value: PageSizeOption) => {
		if (Number.isNaN(value) || value <= 0) return;
		pagination.value = {
			...pagination.value,
			pageSize: value,
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

const normalizeRounds = (rounds: unknown): Round[] => {
	if (!Array.isArray(rounds)) {
		return [];
	}
	return (rounds as Round[]).filter(
		(round): round is Round => typeof round === "object" && round !== null,
	);
};

const formatInterviewDate = (value: Interview["date"]) => {
	if (!value) {
		return "—";
	}
	const timestamp = Date.parse(String(value));
	if (Number.isNaN(timestamp)) {
		return "—";
	}
	return new Date(timestamp).toLocaleString("en-US", {
		day: "numeric",
		month: "short",
		year: "numeric",
	});
};

const getLeetcodeId = (row: Row<Interview>): string | null => {
	const value = row.getValue("leetcodeId");
	if (typeof value !== "string" || value.length === 0) {
		return null;
	}
	return value;
};

const buildLeetcodeLink = (leetcodeId: string) =>
	`https://leetcode.com/discuss/post/${leetcodeId}/`;

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
        <template #expand-cell="{ row }">
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevron-down"
            square
            aria-label="Expand"
            :ui="{
              leadingIcon: [
                'transition-transform',
                row.getIsExpanded() ? 'duration-200 rotate-180' : '',
              ],
            }"
            @click="row.toggleExpanded()"
          />
        </template>

        <template #company-header="{ column }">
          <SortableHeader label="Company" :column="column" />
        </template>
        <template #role-header="{ column }">
          <SortableHeader label="Role" :column="column" />
        </template>
        <template #yoe-header="{ column }">
          <SortableHeader label="YoE" :column="column" />
        </template>
        <template #roundCount-header="{ column }">
          <SortableHeader label="# of Rounds" :column="column" />
        </template>
        <template #date-header="{ column }">
          <SortableHeader label="Date" :column="column" />
        </template>

        <template #leetcodeId-cell="{ row }">
          <template v-for="leetcodeId in [getLeetcodeId(row)]" :key="`leetcode-${row.id}`">
            <a
              v-if="leetcodeId"
              :href="buildLeetcodeLink(leetcodeId)"
              target="_blank"
              rel="noopener noreferrer"
              class="text-primary hover:underline"
            >
              {{ leetcodeId }}
            </a>
            <span v-else>-</span>
          </template>
        </template>

        <template #roundCount-cell="{ row }">
          <template v-for="count in [Number(row.getValue('roundCount')) || 0]" :key="`roundCount-${row.id}`">
            <span class="text-sm font-semibold">
              {{ count }}
            </span>
          </template>
        </template>

        <template #roundDetails-cell="{ row }">
          <template v-for="rounds in [normalizeRounds(row.original?.rounds)]" :key="`roundDetails-${row.id}`">
            <div class="flex flex-wrap gap-2">
              <template v-if="rounds.length">
                <RoundChip
                  v-for="(round, index) in rounds"
                  :key="round?.id ?? `${row.id}-round-${index}`"
                  :round="round"
                />
              </template>
              <span v-else class="text-xs text-gray-500">
                No question details
              </span>
            </div>
          </template>
        </template>

        <template #date-cell="{ row }">
          {{ formatInterviewDate(row.original?.date) }}
        </template>

        <template #expanded="{ row }">
          <Rounds :rounds="(row.original?.rounds ?? []) as Round[]" />
        </template>
      </UTable>
    </div>

  </div>
</template>
