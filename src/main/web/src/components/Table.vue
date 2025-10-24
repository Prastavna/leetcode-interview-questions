<script setup lang="ts">
import type { TableColumn } from "@nuxt/ui";
import { computed, defineProps, h, ref, resolveComponent, watch, withDefaults } from "vue";
import { getPaginationRowModel, type HeaderContext } from "@tanstack/vue-table";
import type { Interview, Round } from "../types/Interview";
import Rounds from "./Rounds.vue";
import RoundChip from "./RoundChip.vue";

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

const UButton = resolveComponent("UButton");

const SORT_ICON_CLASS = "text-[0.65rem] font-medium text-gray-400";

const withColumnWidth = (width: string) => ({
	style: {
		th: { width },
		td: { width },
	},
});

const createSortableHeader = (label: string) => ({ column }: HeaderContext<Interview, unknown>) => {
	if (!column.getCanSort()) return label;
	const direction = column.getIsSorted();
	const symbol = direction === "asc" ? "↑" : direction === "desc" ? "↓" : "↕";
	return h(
		"button",
		{
			type: "button",
			class: "flex items-center gap-1 uppercase tracking-wide text-xs font-semibold text-gray-600",
			onClick: (event: MouseEvent) => {
				event.preventDefault();
				column.toggleSorting(undefined, event.shiftKey);
			},
		},
		[h("span", null, label), h("span", { class: SORT_ICON_CLASS }, symbol)],
	);
};

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

const PAGE_SIZE_OPTIONS = [10, 25, 50, 100, 10000];

const pageSizeItems = PAGE_SIZE_OPTIONS.map((value) => ({
	label: value === 10000 ? "All" : String(value),
	value,
}));

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

const columns: TableColumn<Interview>[] = [
	{
		id: "expand",
		cell: ({ row }) =>
			h(UButton, {
				color: "neutral",
				variant: "ghost",
				icon: "i-lucide-chevron-down",
				square: true,
				"aria-label": "Expand",
				ui: {
					leadingIcon: [
						"transition-transform",
						row.getIsExpanded() ? "duration-200 rotate-180" : "",
					],
				},
				onClick: () => row.toggleExpanded(),
			}),
		meta: withColumnWidth("4%"),
	},
	{
		accessorKey: "leetcodeId",
		header: "#",
		cell: ({ row }) => {
			const leetcodeId = row.getValue("leetcodeId") as string | undefined;
			if (!leetcodeId) {
				return "-";
			}
			return h(
				"a",
				{
					href: `https://leetcode.com/discuss/post/${leetcodeId}/`,
					target: "_blank",
					rel: "noopener noreferrer",
					class: "text-primary hover:underline",
				},
				leetcodeId,
			);
		},
		meta: withColumnWidth("6%"),
	},
	{
		accessorKey: "company",
		header: createSortableHeader("Company"),
		enableSorting: true,
		meta: withColumnWidth("14%"),
	},
	{
		accessorKey: "role",
		header: createSortableHeader("Role"),
		enableSorting: true,
		meta: withColumnWidth("14%"),
	},
	{
		accessorKey: "yoe",
		header: createSortableHeader("YoE"),
		enableSorting: true,
		sortingFn: "basic",
		meta: withColumnWidth("6%"),
	},
	{
		id: "roundCount",
		accessorFn: (row) => (Array.isArray(row.rounds) ? row.rounds.length : 0),
		header: createSortableHeader("# of Rounds"),
		enableSorting: true,
		sortingFn: "basic",
		cell: ({ row }) => {
			const count = Number(row.getValue("roundCount")) || 0;
			return h("span", { class: "text-sm font-semibold" }, count.toString());
		},
		meta: withColumnWidth("10%"),
	},
	{
		id: "roundDetails",
		header: "Round Types",
		cell: ({ row }) => {
			const rounds = (row.original.rounds ?? []) as Round[];
			const chipNodes = (Array.isArray(rounds) ? rounds : []).map((round, index) =>
				h(RoundChip, {
					key: round?.id ?? index,
					round,
				}),
			);
			return h("div", { class: "flex flex-wrap gap-2" },
				chipNodes.length
					? chipNodes
					: [h("span", { class: "text-xs text-gray-500" }, "No question details")],
			);
		},
		meta: withColumnWidth("40%"),
	},
	{
		id: "date",
		accessorFn: (row) => {
			const timestamp = Date.parse(String(row.date));
			return Number.isNaN(timestamp) ? 0 : timestamp;
		},
		header: createSortableHeader("Date"),
		enableSorting: true,
		sortingFn: "datetime",
		cell: ({ row }) => {
			const rawDate = row.original.date;
			if (!rawDate) return "—";
			return new Date(rawDate).toLocaleString("en-US", {
				day: "numeric",
				month: "short",
				year: "numeric",
			});
		},
		meta: withColumnWidth("6%"),
	},
];

const expanded = ref<Record<string, boolean>>({});
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
      >
        <template #expanded="{ row }">
          <Rounds :rounds="(row.original?.rounds ?? []) as Round[]" />
        </template>
      </UTable>
    </div>

  </div>
</template>
