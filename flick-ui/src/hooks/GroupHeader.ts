import { VendaResponse } from "../types/domain";

export interface GroupHeader {
    isGroupHeader: true;
    groupKey: string;
    title: string;
    value: number;
    itemCount: number;
}

export type TableRow = VendaResponse | GroupHeader;