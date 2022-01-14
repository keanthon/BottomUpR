Introduction
Our class’ introduction to program synthesis came in the form of R data transformations, using a standard top-down approach to syntax guided searching of transformations in the tidyr library. We used AST implementations and programming by example methods to search for an R transformation program that would take an input data frame and process it into a given output data frame. The synthesizer would fill holes in the AST representation of an R program, completing various versions of R programs until it eventually found a complete program that satisfied the input and output examples.
We then implemented various optimizations on this search algorithm, to increase both the speed of finding a satisfying program and the accuracy of finding a program that met the intended needs of the user, rather than just finding any possible program that would satisfy the examples. Specifically, we used methods such as bounding the search by AST depth, as well as pruning AST’s that would nest two of the same R operator in a row, as transformations like this could often be completed by much simpler programs. We finished our optimizations by utilizing the Z3 SMT solver to more aggressively prune AST’s that had no chance of satisfying the input and output examples.
With each step of optimizations, we were able to see drastic improvements in both runtime and accuracy. However, several edge cases still existed where runtime and accuracy didn’t improve, even with aggressive pruning. For these cases, the synthesis time for moderately complex transformations would run into the range of several minutes, computing terribly high amounts of AST iterations.
In the interest of tackling these problematic edge cases, we propose a bottom-up approach to program search and synthesis. We also implemented a new version of the tidyr program synthesizer, calling it BottomR. In our approach, we leverage prior work in the form of TF-Coder (Shi et al. 2020). TF-Coder, a program synthesis approach to Tensorflow manipulations, was built on top of the bottom-up enumerative approach of Transit. This approach utilizes a bottom-up search of the program space, using weighted enumeration of operators in the DSL.
Our program BottomR adapts and builds on the TF-Coder approach to synthesize data transformations in R. Specifically, we adapt the weighted enumeration implementation to allow for a guided search of R transformations, allowing the search algorithm to prioritize the most likely, least complex programs first. In addition, we introduced a dynamic programming based optimization to the bottom-up approach, speeding up the evaluation of programs that contain smaller programs that had already been considered in the search.
As a complement to our synthesis algorithm, we also developed a test case randomizer, which automates the process of generating input and output data frame examples to benchmark our algorithm. When we evaluated our approach and its optimizations, we used this randomizer to generate benchmarks. We then tested our entire algorithm’s effectiveness against that of the top down approach, as well as testing our algorithm without each of the aforementioned optimizations to judge their effectiveness on the search time.
Motivating Example

	As previously mentioned, even when considering optimizations such as pruning and bounding, several edge cases still exist where a top down approach to synthesis of R transformations falls short in regard to finding a solution quickly. In the above example, synthesis of a transformation program for the given in/out examples takes an astounding 282,787 milliseconds, over 122,643 iterations. Furthermore, this test case was run with a CFG limited to only three tidyr operators (gather, unite, spread). Had the CFG been expanded to the standard search space identified for our program–including five operators–this search time would have increased exponentially.
	In our exploration of this problem, we identified a few key areas for potential improvement. The first to jump out is the astronomically high amount of iterations run through by the top-down algorithm. Over 100,000 iterations to find a satisfying program that is only 3 nesting layers deep is not serviceable practically. A sufficiently efficient algorithm for searching would need to find a satisfying solution much earlier in the search space for an acceptable runtime.
	Next, it is often unavoidable to have high iteration counts for transformations with high levels of nesting depth. In order to speed up these searches, an optimized program would need to spend less time on each explicit iteration, thus speeding up the overall search. This way, even in high-iteration searches, our algorithm will find satisfying programs in minimal time.

Synthesis
In the next few sections, we will break down the specific optimizations we made to the search algorithm to improve runtime and accuracy. Specifically, we will further address our weighted enumerative search and the dynamic programming implementation, as these were the most fruitful optimizations to our overall runtime. We will then explain how each of these optimizations were implemented into BottomR.

Weighted Enumeration
	Adapting an idea from TF-Coder (Shi et al. 2020), BottomR enumerates R transformations in increasing order by a value referred to as weight. In the context of R transformations, the weight of a program reflects both its complexity and its likeliness to be the intended program based on the input and output examples. In TF-Coder, operators are initialized with a specific weight based on their occurrence in real-life usage of tensorflow manipulations. Then, a major optimization TF-Coder makes to this weighted search is the incorporation of natural language processing. The user looking for a tensorflow expression would provide a natural language description of the specific transformation they’re looking for, and TF-Coder would use this description to re-modify the weights of the operators in the search space. This reweighting essentially guides the search algorithm toward the desired program, allowing the desired program to be found earlier in the search space, while also prioritizing the least complex program.
	BottomR adapts this idea for an application to R transformations, while also optimizing the approach for the different contexts. The first major difference is that R transformations in the tidyr and dplyr libraries can be prioritized or de-prioritized without the use of NLP. Instead, BottomR leverages the fact that the input and output examples can be very telling about which R operators may or may not appear in the transformation expression being searched for. Instead of prompting the user for additional input, the reweight algorithm in BottomR searches for patterns in the input and output examples that indicate the presence or absence of the operators designated in the context-free grammar being used. Performing this reweight before the synthesizer begins searching allows for guidance of the search algorithm based on patterns in the input/output examples. Team member Andrew Fong took lead on this optimization, arriving at a reweight implementation based on the following workflow.

	Take, for instance, the two tables above as input/output examples. The reweight optimization in BottomR would examine these two tables, searching specifically for patterns such as the one highlighted in the above tables. Following the red indicators from the input example to the output, we notice that some column names in the input example appear as cell values in the output example. Per the tidyr library, we now know that the operator known as gather is extremely likely to appear in the desired program as the data transformation observed is in line with gather’s functionality. This observation is just as easy to make with the complement to gather, known as spread:

	In these two above tables, the exact opposite effect can be observed. Now, cell values in the input example are seen as column names in the output example. When reweighting the spread operator, BottomR will search for this type of pattern in the data frames and adjust the weights accordingly.

	See above the pseudocode representation of the reweight implementation, demonstrating the ideology behind weighting the two operators from the examples above. At a high level, the weighting heuristics for all of the operators included in the CFG are based on observed patterns between the input and output examples. These patterns are used to assign higher weights to operators deemed unlikely to appear in the desired program, while assigning lower weights to operators that are more likely.
	These weights are then factored into the search order of the synthesizer. This implementation also plays into the dynamic programming implementation.

Dynamic Programming
	As earlier stated, the primary goal of the dynamic programming optimization is to eliminate the processing time of evaluating expressions that have already been previously evaluated. For example, if the synthesizer is evaluating an expression that is five nesting layers deep, it must calculate five new data frames–one for each nested operator. However, if the first four nested operators have already been evaluated and a resulting data frame already exists in memory, then only one operator calculation is needed to evaluate the entire program.
	To allow for efficient utilization of dynamic programming, team member Anthony implemented BottomR to search through programs in the order of increasing complexity, denoted by weight, and store the weights and output data frames of every program it searches through. It. As the inner programs are of lower complexity than the current program it is working on, it would have been already stored in the SavedValues dictionary when trying to evaluate the more complex current program. For example, slice(x,3,2) is less complex than unite(slice(x,3,2), tmp1, 1, 2) and therefore assigned less weight. When we try to evaluate unite(slice(x,3,2), tmp1, 1, 2), we would have already evaluated slice(x,3,2). Therefore, we can just take the previously stored output of slice(x,3,2) and evaluate unite(output, tmp1, 1, 2) instead of computing the entire program.

See above various examples of programs that would be stored during the search algorithm to find the desired program. Evaluated programs are stored specifically in a hash map, indexing from weight to a SavedVal data structure that contains a rprogram and its resulting output data frame. To expand a program is to add another nesting layer to it, evaluating the program nested inside of each operator in the CFG.
This weight-based indexing then allows the synthesizer to enumerate expansions of the saved programs in the order of increasing weights that have been saved. That is, if a program comes out to be weight 9, the synthesizer won’t expand that program again until all programs of weight eight or lower have been evaluated and expanded.





BottomR Synthesizer
The following figure shows the pseudocode of the BottomR synthesizer.


Line 4 to line 9 reweights each operator based on the input and output and evaluates f(x) and adds them to the Saved dictionary as the initial building blocks of the dynamic programming process.
W denotes the weight of the current program to be investigated, starting from 1 onwards. For each function to be nested on top of the previous nest of program, we compute W-f.weight and index into the dictionary of previously stored programs using that desired weight value since we want their total weight value to add up to W. For each program with the desired weight, we nest f on top of it and check through every permutation of the constants that is available to f to see if those sets of values evaluate to the output. If it does, we return the nested program and corresponding constants. Otherwise, we save it to the dictionary and continue. IntermediateDF denotes the output dataframe of the saved intermediate rprogram and CrossProductConstant denotes the function that produces all possible permutations of constants.
There are a few additional optimizations noticed by teammate Anthony during his implementation of the synthesizer in addition to weighting and dynamic programming which significantly improved runtime. Firstly, it is noticed that column names and numbers almost never repeat. In the cases that they do, the program serves no practical purpose. For example, unite(x, tmp1, 1, 1) will just rename the existing column 1 to tmp1 instead of serving its intended purpose of collating the columns. As a result, the cross product function produces all permutations of the constants without repetition, drastically reducing the amount of programs needed to be evaluated. Secondly, repeated nesting of the same function e.g. unite(unite(x)) is deprioritized by adding a penalty weight (on line 18) when it is saved, causing it to be explored later during the bottom up enumeration process. Thirdly, it is noticed that the permutations of constants are dependent only on the operator, so it can be preprocessed and saved in the synthesizer class to allow quick access instead of doing cross product each time during the main synthesizer loop.



Context Free Grammar (CFG)
We extended the context free grammar in the assignment to include two more operators, namely slice and select, and extended the production to include oldRowNum so it can be utilized by slice when performing its operation. The figure below shows our updated context-free grammar.


Evaluation
In order to evaluate our solution, we ran our synthesizer side-by-side with the most highly-optimized top-down solution, Synthesizer3, on a series of benchmarks obtained from our random test case generator. We tested each algorithm on test cases with nesting depths of one to three layers. Nesting depth refers to the number of functions a program has. For example, unite(gather(x,tmp1,tmp2,3,1),tmp1,2,3) has two nesting layers.
For each layer, we generated five benchmark tests and averaged the runtimes. BottomUp manages to outperform Synthesizer3 on 2 out of the 5 benchmarks with 3 nesting layers and 1 out of 5 benchmarks with 2 nesting layers and all of the benchmarks with 1 nesting layer.

Runtime (milliseconds)
BottomR
Top-Down
1-Deep
29
305
2-Deep
5739
1669
3-Deep
48872
34060


As can be seen above, on the average case, BottomR performs worse than the top-down approach except in simple cases of one nesting layer. When the desired R program is more complex than a single nesting layer, BottomR can be expected to run slower than its top-down counterpart on the average case. However, in cases such as the edge case mentioned in the motivating example, BottomR was found to run faster by a factor of 20, finding the desired program in just 13,391 milliseconds, compared to 282,787. We found that in all edge cases we tested where the top-down approach would run significantly slowly, BottomR would still find a solution in amounts of time consistent with general runtime associated with that nesting depth. Therefore, while BottomR did not immediately succeed in finding solutions faster than the top-down approach, it did generally succeed in eliminating extreme edge cases.
We also ran the same benchmarks on a separate version of BottomR with its dynamic programming feature stripped, to evaluate the true effectiveness of the dynamic programming optimization.

Runtime (milliseconds)
BottomR
BottomR (no DP)
1-Deep
29
41
2-Deep
5739
9856
3-Deep
48872
97841


As is evident in the above results, the dynamic programming optimization was highly effective in improving the runtime performance of BottomR. In the average case, the DP implementation improved runtime by about a factor of two. Since the search algorithms are otherwise identical, and the search order remains consistent, the amount of iterations of each search is also the same. Given that the runtime is significantly faster with the same number of iterations, we conclude that the DP optimization succeeded in reducing the computation time of each iteration, removing unnecessary redundant expression evaluations.

Future Work
	There are still many more areas for improvement of BottomR. A key area identified by our team is the inconsistency of solving average cases quickly. We surmise that with more extensive design to the reweighting algorithm, we would be better able to guide the search algorithm of BottomR, thus being able to find satisfying solutions more quickly.
	Another idea that we have is that the BottomR synthesizer can be further extended to interpret the input and output and generate an efficient CFG automatically. Thus, reducing the search space and improving the runtime. In order to build a more robust system, we can also look to include even more operators in our CFG and weighting function, and run even more test cases without the time constraint.
	To generalize further, BottomR and its accompanying data structures can be further built to interpret CFG from any domain instead of just R table transformations. We have seen that bottom up weight enumerated programming is applicable for both R transformation and Tensorflow transformation. Generalizing to all domain transformations is the next logical step.

Appendix: Random Test Case Generator
	We build a test case generator that randomly generates programs to be used as test cases and its corresponding output with specified nesting layers given an input and CFG. The Random Test Generator will randomly select a specified number of programs and randomly choose a permutation of constants from the cross product list of all permutations saved in the synthesizer. The Generator then evaluates the output of the generated program and ensures that it does not contain NA and it is not empty. If those constraints are not met, the process is repeated until they are met.
