export const INTERVIEW_DATASET_FILENAME = "interviews.json";

export const buildInterviewsDatasetUrl = (baseUrl?: string) => {
	const base = typeof baseUrl === "string" && baseUrl.length > 0 ? baseUrl : "/";
	const normalized = base.endsWith("/") ? base : `${base}/`;
	return `${normalized}${INTERVIEW_DATASET_FILENAME}`;
};

export const toDateInputString = (value: Date | string | null | undefined) => {
	if (!value) return null;
	const date = value instanceof Date ? value : new Date(value);
	if (Number.isNaN(date.getTime())) return null;
	const offset = date.getTimezoneOffset();
	const local = new Date(date.getTime() - offset * 60000);
	return local.toISOString().slice(0, 10);
};
