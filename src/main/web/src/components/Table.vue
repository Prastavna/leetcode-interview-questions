<script setup lang="ts">
import type { TableColumn } from "@nuxt/ui";
import { defineProps, h, ref, resolveComponent, withDefaults } from "vue";
import type { Interview, Round } from "../types/Interview";
import { QuestionType } from "../types/Interview";
import Chip from "./Chip.vue";
import Rounds from "./Rounds.vue";

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
		cell: ({ row }) => {
			const rounds = (row.getValue("rounds") as Round[]) ?? [];
			const count = Array.isArray(rounds) ? rounds.length : 0;
			const chipNodes = (Array.isArray(rounds) ? rounds : []).map((round, index) => {
				const uniqueLabels = new Set<string>();
				(round?.questions ?? []).forEach((question) => {
					if (!question) return;
					const typeKey = question.type as keyof typeof QuestionType | undefined;
					const label = typeKey && QuestionType[typeKey] ? QuestionType[typeKey] : question.type;
					if (label) uniqueLabels.add(label as string);
				});
				const segments = Array.from(uniqueLabels);
				return h(Chip, {
					key: round?.id ?? index,
					segments,
					text: segments.length ? undefined : "—",
				});
			});
			return h("div", { class: "flex flex-col gap-2" }, [
				h("span", { class: "text-sm font-semibold text-gray-900" }, count.toString()),
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

const globalFilter = ref("");
const expanded = ref<Record<string, boolean>>({});
</script>

<template>
  <div class="flex flex-col flex-1 w-full">
    <div class="flex px-4 py-3.5 border-b border-accented">
      <UInput v-model="globalFilter" class="max-w-sm" placeholder="Filter..." />
    </div>

    <div class="p-4" v-if="props.isLoading">Loading interviews…</div>
    <div class="p-4 text-red-600" v-else-if="props.error">{{ props.error }}</div>
    <UTable
      v-else
      v-model:expanded="expanded"
      ref="table"
      v-model:global-filter="globalFilter"
      :data="props.data"
      :columns="columns"
    >
      <template #expanded="{ row }">
        <Rounds :rounds="row.getValue('rounds') as Round[]" />
      </template>
    </UTable>
  </div>
</template>
