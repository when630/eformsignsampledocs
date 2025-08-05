export type Document = {
  id: number;
  title: string;
};

export type Category = {
  id: number;
  name: string;
  children: Category[];
};