public abstract class Evaluation_ImplBase<ITEM_TYPE, STATS_TYPE> {
  protected abstract CollectionReader getCollectionReader(List<ITEM_TYPE> items);
  protected abstract void train(CollectionReader collectionReader, File directory);
  protected abstract STATS_TYPE test(CollectionReader collectionReader, File directory);

  public Evaluation_ImplBase(File baseDirectory) { ... }
  public STATS_TYPE trainAndTest(List<ITEM_TYPE> trainItems, List<ITEM_TYPE> testItems) { ... }
  public List<STATS_TYPE> crossValidation(List<ITEM_TYPE> items, int folds) { ... }
}
