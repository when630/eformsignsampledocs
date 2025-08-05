export type Document = {
  id: number;
  title: string;
  storageId: number;
};

export type Category = {
  id: number;
  name: string;
  children: Category[];
};