<script setup lang="ts">
import { computed } from "vue";
import type { Column } from "@tanstack/vue-table";
import type { Interview } from "../types/Interview";

const props = defineProps<{
	label: string;
	column: Column<Interview, unknown>;
}>();

const buttonClass =
	"flex items-center gap-1 uppercase tracking-wide text-xs font-semibold text-gray-600";
const iconClass = "text-[0.65rem] font-medium text-gray-400";

const sortIcon = computed(() => {
	const direction = props.column.getIsSorted();
	if (direction === "asc") {
		return "↑";
	}
	if (direction === "desc") {
		return "↓";
	}
	return "↕";
});

const handleClick = (event: MouseEvent) => {
	event.preventDefault();
	props.column.toggleSorting(undefined, event.shiftKey);
};
</script>

<template>
	<span v-if="!column.getCanSort()" :class="buttonClass">
		{{ label }}
	</span>
	<button v-else type="button" :class="buttonClass" @click="handleClick">
		<span>{{ label }}</span>
		<span :class="iconClass">{{ sortIcon }}</span>
	</button>
</template>
