import { h, resolveComponent } from "vue";
import type { TableColumn } from "@nuxt/ui";
import type { HeaderContext } from "@tanstack/vue-table";
import type { Interview, Round } from "../../types/Interview";
import RoundChip from "../../components/RoundChip.vue";

const SORT_ICON_CLASS = "text-[0.65rem] font-medium text-gray-400";

const withColumnWidth = (width: string) => ({
	style: {
		th: { width },
		td: { width },
	},
});

const createSortableHeader =
	(label: string) =>
	({ column }: HeaderContext<Interview, unknown>) => {
		if (!column.getCanSort()) return label;
		const direction = column.getIsSorted();
		const symbol = direction === "asc" ? "↑" : direction === "desc" ? "↓" : "↕";

		return h(
			"button",
			{
				type: "button",
				class: "flex items-center gap-1 uppercase tracking-wide text-xs font-semibold text-gray-600",
				onClick: (event: MouseEvent) => {
					event.preventDefault();
					column.toggleSorting(undefined, event.shiftKey);
				},
			},
			[h("span", null, label), h("span", { class: SORT_ICON_CLASS }, symbol)],
		);
	};

export const PAGE_SIZE_OPTIONS = [10, 25, 50, 100, 10000] as const;

export const createPageSizeItems = () =>
	PAGE_SIZE_OPTIONS.map((value) => ({
		label: value === 10000 ? "All" : String(value),
		value,
	}));

export const buildInterviewColumns = (): TableColumn<Interview>[] => {
	const UButton = resolveComponent("UButton");

	return [
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
			meta: withColumnWidth("4%"),
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
			meta: withColumnWidth("6%"),
		},
		{
			accessorKey: "company",
			header: createSortableHeader("Company"),
			enableSorting: true,
			meta: withColumnWidth("14%"),
		},
		{
			accessorKey: "role",
			header: createSortableHeader("Role"),
			enableSorting: true,
			meta: withColumnWidth("14%"),
		},
		{
			accessorKey: "yoe",
			header: createSortableHeader("YoE"),
			enableSorting: true,
			sortingFn: "basic",
			meta: withColumnWidth("6%"),
		},
		{
			id: "roundCount",
			accessorFn: (row) => (Array.isArray(row.rounds) ? row.rounds.length : 0),
			header: createSortableHeader("# of Rounds"),
			enableSorting: true,
			sortingFn: "basic",
			cell: ({ row }) => {
				const count = Number(row.getValue("roundCount")) || 0;
				return h("span", { class: "text-sm font-semibold" }, count.toString());
			},
			meta: withColumnWidth("10%"),
		},
		{
			id: "roundDetails",
			header: "Round Types",
			cell: ({ row }) => {
				const rounds = (row.original.rounds ?? []) as Round[];
				const chipNodes = (Array.isArray(rounds) ? rounds : []).map((round, index) =>
					h(RoundChip, {
						key: round?.id ?? index,
						round,
					}),
				);
				return h(
					"div",
					{ class: "flex flex-wrap gap-2" },
					chipNodes.length
						? chipNodes
						: [h("span", { class: "text-xs text-gray-500" }, "No question details")],
				);
			},
			meta: withColumnWidth("40%"),
		},
		{
			id: "date",
			accessorFn: (row) => {
				const timestamp = Date.parse(String(row.date));
				return Number.isNaN(timestamp) ? 0 : timestamp;
			},
			header: createSortableHeader("Date"),
			enableSorting: true,
			sortingFn: "datetime",
			cell: ({ row }) => {
				const rawDate = row.original.date;
				if (!rawDate) return "—";
				return new Date(rawDate).toLocaleString("en-US", {
					day: "numeric",
					month: "short",
					year: "numeric",
				});
			},
			meta: withColumnWidth("6%"),
		},
	];
};

export const interviewRowIdAccessor = (row: Interview, index: number) => {
	const identifier = row.id || row.leetcodeId;
	if (identifier) {
		return identifier;
	}
	return `${row.company ?? "company"}-${row.role ?? "role"}-${row.date ?? index}-${index}`;
};
