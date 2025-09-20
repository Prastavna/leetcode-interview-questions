<script setup lang="ts">
import type { TableColumn } from "@nuxt/ui";
import { computed, defineProps, h, ref, resolveComponent, watch, withDefaults } from "vue";
import { getPaginationRowModel } from "@tanstack/vue-table";
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

const pagination = ref({
	pageIndex: 0,
	pageSize: 25,
});

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
	},
	{
		accessorKey: "company",
		header: "Company",
	},
	{
		accessorKey: "role",
		header: "Role",
	},
	{
		accessorKey: "yoe",
		header: "YoE",
	},
	{
		accessorKey: "rounds",
		header: "# of Rounds",
    cell: ({ row }) => `${(row.getValue("rounds") as Round[]).length}`
	},
  {
		accessorKey: "rounds",
		header: "Round Types",
		cell: ({ row }) => {
			const rounds = (row.getValue("rounds") as Round[]) ?? [];
			const chipNodes = (Array.isArray(rounds) ? rounds : []).map((round, index) =>
				h(RoundChip, {
					key: round?.id ?? index,
					round,
				}),
			);
			return h("div", { class: "flex flex-col gap-2" }, [
				chipNodes.length
					? h("div", { class: "flex flex-wrap gap-2" }, chipNodes)
					: h("span", { class: "text-xs text-gray-500" }, "No question details"),
			]);
		},
  },
	{
		accessorKey: "date",
		header: "Date",
		cell: ({ row }) => {
			return new Date(row.getValue("date")).toLocaleString("en-US", {
				day: "numeric",
				month: "short",
				year: "numeric",
      });
		},
	},
];

const expanded = ref<Record<string, boolean>>({});
</script>

<template>
  <div class="flex flex-col flex-1 w-full">
    <div class="p-4" v-if="props.isLoading">Loading interviews…</div>
    <div class="p-4 text-red-600" v-else-if="props.error">{{ props.error }}</div>
    <UTable
      v-else
      v-model:expanded="expanded"
      v-model:pagination="pagination"
      ref="table"
      :data="props.data"
      :columns="columns"
      :pagination-options="paginationOptions"
      class="[&_td]:!py-1"
    >
      <template #expanded="{ row }">
        <Rounds :rounds="row.getValue('rounds') as Round[]" />
      </template>
    </UTable>

    <div class="flex flex-col gap-3 px-4 py-3 border-t border-accented sm:flex-row sm:items-center sm:justify-between">
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
  </div>
</template>
