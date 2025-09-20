<template>
  <Table :data="data" :is-loading="isLoading" :error="error" />
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import Table from "../components/Table.vue";
import type { Interview } from "../types/Interview";

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
</script>
