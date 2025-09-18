<script setup lang="ts">
import type { TableColumn } from "@nuxt/ui";
import { h, onMounted, ref, resolveComponent } from "vue";
import type { Interview } from "../types/Interview";

const UButton = resolveComponent("UButton");

// Table data state
const data = ref<Interview[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetch("/interviews.json", { cache: "no-store" });
    if (!res.ok) throw new Error(`Failed to load interviews.json (${res.status})`);
    const json = await res.json();
    data.value = Array.isArray(json) ? (json as Interview[]) : [];
  } catch (e: any) {
    error.value = e?.message ?? "Unknown error loading data";
  } finally {
    isLoading.value = false;
  }
});

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
		accessorKey: "id",
		header: "#",
		cell: ({ row }) => `${row.getValue("id")}`,
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
			return row.getValue("rounds").length;
		},
	},
	{
		accessorKey: "date",
		header: "Date",
		cell: ({ row }) => {
			return new Date(row.getValue("date")).toLocaleString("en-US", {
				day: "numeric",
				month: "short",
				hour: "2-digit",
				minute: "2-digit",
				hour12: false,
			});
		},
	},
];

const globalFilter = ref("");
const expanded = ref({ 1: true });
</script>

<template>
  <div class="flex flex-col flex-1 w-full">
    <div class="flex px-4 py-3.5 border-b border-accented">
      <UInput v-model="globalFilter" class="max-w-sm" placeholder="Filter..." />
    </div>

    <div class="p-4" v-if="isLoading">Loading interviews…</div>
    <div class="p-4 text-red-600" v-else-if="error">{{ error }}</div>
    <UTable
      v-else
      v-model:expanded="expanded"
      ref="table"
      v-model:global-filter="globalFilter"
      :data="data"
      :columns="columns"
    >
      <template #expanded="{ row }">
        <UContainer>
          <div v-for="round in row.getValue('rounds')">
            <div v-for="question in round.questions">
              <p>{{ question.type }}</p>
              <p>{{ question.content }}</p>
            </div>
          </div>
        </UContainer>
      </template>
    </UTable>
  </div>
</template>
