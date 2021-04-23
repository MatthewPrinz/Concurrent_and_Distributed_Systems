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
            String line = value.toString().toLowerCase();
            String[] words = line.split("[^a-zA-Z0-9]+");

            // Delete repeating characters from string array
            HashSet<String> wordsSet = new HashSet<String>(Arrays.asList(words));
            words = wordsSet.toArray(new String[wordsSet.size()]);
//            System.out.println("words are: " + Arrays.toString(words));

            for (int i = 0; i < words.length; i++)
            {
                for (int j = 0; j < words.length; j++) {
                    if (i != j)
                    {
			Tuple edge = new Tuple(new Text(words[j]), new IntWritable(1));
                	context.write(new Text(words[i]), edge);
//			System.out.printf("edge is: %s -> %s %s\n", words[i], edge.getKey(), edge.getValue());
                    }
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
            Map<String, IntWritable> map = new HashMap<>();
		
//System.out.println("current key is " + key.toString());
            for (Tuple tuple : values)
            {
		String dstText = tuple.getKey().toString();
		System.out.printf("One tuple is: %s %s\n", tuple.getKey(), tuple.getValue());
                if (map.containsKey(tuple.getKey().toString()))
                {
                    // IntWritable tupVal = tuple.getValue();
                    // IntWritable mapVal = map.get(tuple.getKey());
                    // int combVal = tupVal.get() + mapVal.get();

                    map.put(dstText, new IntWritable(map.get(dstText).get()+1));
                }
                else
                {
                    map.put(dstText, new IntWritable(1));
                }
//		System.out.println(map);
            }

            for (Map.Entry<String, IntWritable> me : map.entrySet())
            {
                Tuple edge = new Tuple(new Text(me.getKey()), me.getValue());
		System.out.println("key is " + key.toString() + ", edge is: " + edge);
                context.write(key, edge);
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
                Map<String, IntWritable> map = new HashMap<>();
                for (Tuple tuple : values)
                {
		System.out.printf("key is %s, edge is %s %s\n",key.toString(), tuple.getKey(), tuple.getValue());
			String dstStr = tuple.getKey().toString();
                    if (map.containsKey(tuple.getKey()))
                    {
                        map.put(dstStr, new IntWritable(map.get(dstStr).get() + tuple.getValue().get()));
                    }
                    else
                    {
                        map.put(dstStr, new IntWritable(tuple.getValue().get()));
                    }
		System.out.println(map);
                }
                // Write out the results; you may change the following example
                // code to fit with your reducer function.
                //   Write out each edge and its weight
	            Text value = new Text();
                
                for(String neighbor: map.keySet()) {    
                    String weight = map.get(neighbor).toString();
                    value.set(" " + neighbor + " " + weight);
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


