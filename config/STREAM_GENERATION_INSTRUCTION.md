# Generating processing task stream mixtures

The simulator is able to operate on the synthetically generated tasks (workflows) mixtures.
Synthetic tasks are being generated using the real-life cloud computing data, hence, they may be used to run scenarios
with close-to-real-life environmental conditions.

All tools used to (1) distinguish different groups of tasks (i.e. cluster real-life computing data) and (2)
generate synthetic mixtures, can be found in the module _data-clustering_.

## Preparing configuration

Before proceeding to data clustering and mixture generation, the user needs to provide the path under which the data set
that is going to be the basis for the further analysis, is stored.

In order to do so, the configuration file _.env_ stored in the `./config/data-clustering` directory, should be modified.
It contains two properties: _DATA_DIRECTORY_ and _TEST_DATA_DIRECTORY_:

- _DATA_DIRECTORY_ should be modified in order to provide the path to the input data.
- _TEST_DATA_DIRECTORY_ can be optionally modified (but it is not recommended) in order to pass a
  different path to the directory storing data used in unit testing.

IMPORTANT! Current data scrapping has been implemented based on a specific data model. Therefore, input data should
either adhere to its structure or the user must accordingly modify the code base.

For the information about the data model (and, in general, implemented data clustering) refer to:

```text
Wrona, Z., Ganzha, M., Paprzycki, M., Krzyżanowski S., (2023). 
Analysis of job processing data -- towards large cloud infrastructure operation simulation.,
In: Big Data and Artificial Intelligence. BDA 2023. Lecture Notes in Computer Science, vol 14418. Springer, Cham.
```

## Main tools

The main tools offered by the module include:

- tools for exploratory analysis of the data
- tools for data clustering
- tool for synthetic generation of workflow streams

All tools are executed from Jupyter notebooks within _data-clustering_ module.
Before using any of the tools, it is necessary to compile the module (to do so, refer
to [Compilation Instruction](../compile/COMPILE_INSTRUCTION.md)).

### Exploratory Analysis Tools

The tools are located within the _exploratory-analysis-tools.ipynb_ notebook.
All of them have been implemented as part of the _ExploratoryAnalysis_ class.

The user, may:

1. execute all pre-defined methods, by running function **run_all()** (set by default).
2. execute only selected methods, e.g. **analyze_workflow_steps()**

The parameters, which can be configured within this section include:

- _WORKFLOWS_ - input workflows data frame (optional). There are currently 3 implemented methods which allow to import
  workflows data (default: _WorkflowsImport.IMPORT_FROM_CSV()_):
    - _WorkflowsImport.IMPORT_FROM_JSON()_ - method imports workflows from Argo _.json_ files and combines their data
      with data from the indicated database. The resulting complete workflows are then stored in input _.csv_ so that
      they can be reused in the feature.
    - _WorkflowsImport.IMPORT_FROM_CSV()_ - method imports workflows from default _.csv_ file created after executing _
      WorkflowsImport.IMPORT_FROM_JSON()_ function.
    - _WorkflowsImport.IMPORT_FROM_FILE(file_name)_ - method imports workflows from _.csv_ file indicated by _file_name_
      . Important! The file should be located within _./data-clustering/src/data/input_ directory
- _WORKFLOWS_DB_ - database input workflows - i.e. workflows containing only data from database (optional). These
  workflows can be imported using _WorkflowsImport.IMPORT_FROM_DB()_ method (default method).
- _WORKFLOWS_DISPLAYABLE_DETAILS_ - list of the features of the workflows for which exploratory analysis is to be
  conducted
- _STORE_RESULTS_ - flag indicating if the results should be stored in the output files within
  _./data-clustering/src/data/results/exploratory-results_ directory

### Data Clustering Tools

The data clustering tools are located within _data-clustering-tools.ipynb_ notebook.
The main class inside which all the tools have been implemented is called _Clustering_.
By executing the method **run(...)** the clustering is being conducted.

The user may change the following parameters:

- _WORKFLOWS_ - input workflows data frame (optional).
- _ORDERS_ - input orders data frame (optional). It can be provided by the user in case when the clustering is to be
  conducted on the granuality level of client orders (i.e. collections of workflows). There are 2 methods which allows
  to import orders data:
    - _OrdersImport.IMPORT_FROM_JSON()_ - (similar method to _WorkflowsImport.IMPORT_FROM_JSON()_) method imports orders
      from Argo _.json_ files and combines their data with data from the indicated database.
    - _OrdersImport.IMPORT_FROM_CSV()_ - method imports orders from default _.csv_ file created after executing
      _OrdersImport.IMPORT_FROM_JSON()_ function.
- _CLUSTERING_OBJECTIVE_ - indicates if clustering should be performed for orders or workflows. It's either
  _ClusteringObjective.WORKFLOWS_ or _ClusteringObjective.ORDERS_
- _WORKFLOWS_DISPLAYABLE_DETAILS_ - list of workflows' features that are to be displayed along with clustering outputs
- _ORDERS_DISPLAYABLE_DETAILS_ - list of orders' features that are to be displayed along with clustering outputs
- _CLUSTERING_METHOD_ - selected clustering method. Available methods:
    - **K_MEANS**
    - **BIRCH**
    - **GMM**
    - **FUZZY_C_MEANS**
    - **HDBSCAN**
    - **ICCM**
    - **IClust**
- _CLUSTERING_PARAMS_ - clustering parameters passed to clustering method. List of parameters may differ depending on
  the selected clustering methods. Please refer to the method description to get more information about its acceptable
  parameters.
- _REDUCTION_PARAMS_ - parameters that are used in dimensionality reduction (e.g. in UMAP).
- _DIMENSIONALITY_REDUCTION_ - method used in dimensionality reduction. Available methods:
    - **PCA**
    - **UMAP**
- _PRE_PROCESSING_OPERATIONS_ - list of pre-processing operations that are to be applied prior to the data clustering.
  In current version, 7 methods have been implemented:
    - **ONLY_DB_RECORDS** - use only records from the database (i.e. filter out workflows for which database records are
      missing)
    - **MERGE_STATUSES** - merge detailed and output messages of argo files (more information behind this operation has
      been provided in the aforementioned article).
    - **FILTER_TEST_WORKFLOWS** - filter workflows recognized as testing entries.
    - **FILTER_OUT_DOWNLOAD_WORKFLOWS** - remove workflows of type _download_ from the sample.
    - **TAKE_ONLY_DOWNLOAD_WORKFLOWS** - use only workflows of type _download_.
    - **MERGE_CARD_COH** - merge workflows of having in their name _coh_.
    - **ONLY_SUCCEEDED** - take into account only workflows which succeeded without errors.
- _WORKFLOWS_CLUSTERING_DETAILS_ - set of features used in workflows clustering.
- _ORDER_CLUSTERING_DETAILS_ - set of features used in orders clustering.
- _VALIDATION_METRICS_ - metrics used to evaluate clustering results. Available metrics:
    - **SILHOUETTE**
    - **CALINSKI-HARABASZ**
    - **DAVIES-BOULDIN**
    - **XIE-BENI**
- _TEST_PARAMETERS_ - flag indicating if the clustering should be run for different sets of parameters (i.e. to perform
  hyperparameters tuning)
- _SAVE_RESULTS_ - flag indicating if the output results should be stored
- _CLUSTERING_NAME_ - name of the clustering that is performed (name is going to be used in stored/displayed results).

### Synthetic Streams Generation Tool

Synthetic stream generation tool is located in _data-augmentation-tools.ipynb_ notebook.
Before running the stream generator, it is necessary to perform data clustering, since the structure of the clusters is
going to be used as a basis for Gaussian Mixture Models (GMM).

The user must specify the following input parameters:

- _INPUT_WORKFLOWS_ - list of input, clustered, workflows. The parameter stores a list, since it is possible to generate
  mixture of the workflows based on the clusters resulting from different clusterings (e.g. tasks of type _download_ may
  be clustered separately from the other types of tasks, but they still may be included within the same mixture).
- _LABELS_PER_WORKFLOWS_ - list of lists of labels specifying which clusters are to be selected in the stream
  generation.
  For example, the value: `[[1,2,3], [5,7]]` indicates that only clusters 1,2,3 will be used from the first clustering,
  while clusters 5,7 are to be selected from the second one.
- _SAMPLE_SIZES_ - list of sizes of samples that are to be generated for each cluster type. For example the
  value: `[[10, 20, 5], [3, 4]]` would mean that (referring to the previous example), for the first clustering, based on
  cluster 1 - 10 workflows will be generated; cluster 2 - 20 workflows will be generated and cluster 3 - 5 workflows
  will be generated.

To run the synthetic stream generation, the method **run_and_save()** from _AugmentWorkflows_ should be executed.

The resulting mixed workflow stream is going to be stored under _./data-clustering/src/data/synthetic_ directory.

