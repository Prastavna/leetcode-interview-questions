import { onBeforeUnmount, onMounted, ref } from "vue";
const MEDIA_QUERY = "(max-width: 640px)";

export function useIsMobile() {
  const isMobile = ref(false);
	let mediaQuery: MediaQueryList | null = null;

  const update = () => {
		isMobile.value = Boolean(mediaQuery?.matches);
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
  return isMobile;
}
