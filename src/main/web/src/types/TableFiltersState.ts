export type TableFiltersState = {
	search: string;
	questionTypes: string[];
	yoeRange: [number, number];
	dateRange: {
		from: string | null;
		to: string | null;
	};
};
