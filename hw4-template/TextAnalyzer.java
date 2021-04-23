import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
// import org.graalvm.compiler.phases.common.ExpandLogicPhase;
import java.util.*;
import java.io.IOException;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
        public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
            // Implementation of you mapper function
            String paragraph = value.toString().toLowerCase();
            String[] paragraphArr = paragraph.split("\n");

            // Delete repeating characters from string array
            for (String line : paragraphArr)
            {
                String[] words = line.split("[^a-zA-Z0-9]+");
                HashSet<String> wordsSet = new HashSet<String>(Arrays.asList(words));
                words = wordsSet.toArray(new String[wordsSet.size()]);
                for (int i = 0; i < words.length; i++) 
                {
                    Tuple edge = new Tuple();
                    for (int j = 0; j < words.length; j++) {
                        if (i != j) 
                        {
                            edge.set(new Text(words[j]), new IntWritable(1));
                        }
                    }
                    context.write(new Text(words[i]), edge);
                }
            }
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
        public void reduce(Text key, Iterable<Tuple> values, Context context)
            throws IOException, InterruptedException
        {
            Map<Text, IntWritable> map = new HashMap<>();
            for (Tuple tuple : values)
            {
                if (map.containsKey(tuple.getKey()))
                {
                    IntWritable tupVal = tuple.getValue();
                    IntWritable mapVal = map.get(tuple.getKey());
                    int combVal = tupVal.get() + mapVal.get();
                    map.put(tuple.getKey(), new IntWritable(combVal));
                }
                else
                {
                    map.put(tuple.getKey(), new IntWritable(1));
                }
            }
            for (Map.Entry<Text, IntWritable> me : map.entrySet())
            {
                Tuple combined = new Tuple(me.getKey(), me.getValue());
                context.write(key, combined);
            }
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<Tuple> values, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function
                Map<Text, IntWritable> map = new HashMap<>();
                for (Tuple tuple : values)
                {
                    if (map.containsKey(tuple.getKey()))
                    {
                        IntWritable tupVal = tuple.getValue();
                        IntWritable mapVal = map.get(tuple.getKey());
                        int combVal = tupVal.get() + mapVal.get();
                        map.put(tuple.getKey(), new IntWritable(combVal));
                    }
                    else
                    {
                        map.put(tuple.getKey(), new IntWritable(1));
                    }
                }
                // Write out the results; you may change the following example
                // code to fit with your reducer function.
                //   Write out each edge and its weight
	            Text value = new Text();
                
                for(Text neighbor: map.keySet()) {    
                    String weight = map.get(neighbor).toString();
                    value.set(" " + neighbor.toString() + " " + weight);
                    context.write(key, value);
            }
            //   Empty line for ending the current context key
            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
        Job job = new Job(conf, "mep3368_jp54694"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);
        
	    // set local combiner class
        job.setCombinerClass(TextCombiner.class);
	    // set reducer class        
	    job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Tuple.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception{
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }
}



