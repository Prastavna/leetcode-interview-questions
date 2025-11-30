import { ref, watch } from "vue";

type Theme = "light" | "dark" | "auto";

const THEME_STORAGE_KEY = "vueuse-color-scheme";
const HTML_CLASS_DARK = "dark";

// Get the effective theme (resolves "auto" to actual light/dark based on system preference)
function getEffectiveTheme(theme: Theme): "light" | "dark" {
	if (theme === "auto") {
		if (typeof window === "undefined") return "light";
		const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
		return prefersDark ? "dark" : "light";
	}
	return theme;
}

// Apply theme to HTML element
function applyTheme(effectiveTheme: "light" | "dark") {
	if (typeof window === "undefined") return;
	const html = document.documentElement;
	if (effectiveTheme === "dark") {
		html.classList.add(HTML_CLASS_DARK);
	} else {
		html.classList.remove(HTML_CLASS_DARK);
	}
}

// Initialize theme immediately (before Vue mounts) to prevent flash
function initializeThemeSync(): Theme {
	if (typeof window === "undefined") return "light";

	// Try to get saved preference
	let savedTheme: Theme | null = null;
	try {
		const stored = localStorage.getItem(THEME_STORAGE_KEY);
		if (stored === "light" || stored === "dark" || stored === "auto") {
			savedTheme = stored;
		}
	} catch (e) {
		// Ignore localStorage errors
	}

	// Use saved preference, or fall back to "auto" (system preference)
	const initialTheme: Theme = savedTheme || "auto";
	const effectiveTheme = getEffectiveTheme(initialTheme);
	applyTheme(effectiveTheme);

	return initialTheme;
}

// Initialize theme synchronously on module load
const initialTheme = initializeThemeSync();

export function useTheme() {
	const theme = ref<Theme>(initialTheme);

	// Computed effective theme for display purposes
	const effectiveTheme = ref<"light" | "dark">(getEffectiveTheme(theme.value));
	
	const updateEffectiveTheme = () => {
		effectiveTheme.value = getEffectiveTheme(theme.value);
		applyTheme(effectiveTheme.value);
	};

	// Watch for theme changes
	watch(theme, updateEffectiveTheme, { immediate: true });

	// Watch for system preference changes when theme is "auto"
	if (typeof window !== "undefined") {
		const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
		const handleSystemThemeChange = () => {
			if (theme.value === "auto") {
				updateEffectiveTheme();
			}
		};
		mediaQuery.addEventListener("change", handleSystemThemeChange);
	}

	const setTheme = (newTheme: Theme) => {
		theme.value = newTheme;
		
		try {
			localStorage.setItem(THEME_STORAGE_KEY, newTheme);
		} catch (e) {
			// Ignore localStorage errors (e.g., in private browsing)
		}
	};

	const toggleTheme = () => {
		// Cycle through: light -> dark -> auto -> light
		if (theme.value === "light") {
			setTheme("dark");
		} else if (theme.value === "dark") {
			setTheme("auto");
		} else {
			setTheme("light");
		}
	};

	return {
		theme,
		effectiveTheme,
		setTheme,
		toggleTheme,
	};
}

