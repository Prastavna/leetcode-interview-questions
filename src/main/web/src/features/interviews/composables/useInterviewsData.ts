import { onMounted, ref } from "vue";
import type { Ref } from "vue";
import type { Interview } from "../../../types/Interview";
import { buildInterviewsDatasetUrl } from "../utils";

export type UseInterviewsDataResult = {
	interviews: Ref<Interview[]>;
	isLoading: Ref<boolean>;
	error: Ref<string | null>;
	reload: () => Promise<void>;
};

export const useInterviewsData = (datasetPath?: string): UseInterviewsDataResult => {
	const interviews = ref<Interview[]>([]);
	const isLoading = ref(true);
	const error = ref<string | null>(null);

	const datasetUrl =
		datasetPath ??
		buildInterviewsDatasetUrl(
			// Allow overriding base URL via Vite's `BASE_URL`.
			typeof import.meta !== "undefined" ? import.meta.env.BASE_URL : "/",
		);

	const load = async () => {
		isLoading.value = true;
		error.value = null;
		try {
			const response = await fetch(datasetUrl, { cache: "no-store" });
			if (!response.ok) {
				throw new Error(`Failed to load ${datasetUrl} (${response.status})`);
			}
			const json = await response.json();
			interviews.value = Array.isArray(json) ? (json as Interview[]) : [];
		} catch (err: unknown) {
			const message = err instanceof Error ? err.message : "Unknown error loading data";
			error.value = message;
			interviews.value = [];
		} finally {
			isLoading.value = false;
		}
	};

	onMounted(load);

	return {
		interviews,
		isLoading,
		error,
		reload: load,
	};
};
