import { onBeforeUnmount, onMounted, ref } from "vue";

const MEDIA_QUERY = "(prefers-color-scheme: dark)";

export function usePrefersDark() {
	const isDark = ref(false);
	let mediaQuery: MediaQueryList | null = null;

	const update = () => {
		isDark.value = Boolean(mediaQuery?.matches);
	};

	onMounted(() => {
		if (typeof window === "undefined" || typeof window.matchMedia !== "function") return;
		mediaQuery = window.matchMedia(MEDIA_QUERY);
		update();
		mediaQuery.addEventListener("change", update);
	});

	onBeforeUnmount(() => {
		mediaQuery?.removeEventListener("change", update);
	});

	return isDark;
}
