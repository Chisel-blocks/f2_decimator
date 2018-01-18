// See LICENSE for license details.
// Use handlebars for template generation
//
//Start with a static tb and try to genererate a gnerator for it
package f2_decimator

import chisel3._
import java.io.{File, FileWriter, BufferedWriter}
import com.gilt.handlebars.scala.binding.dynamic._
import com.gilt.handlebars.scala.Handlebars

//Testbench.
object tb_f2_decimator {
  def main(args: Array[String]): Unit = {
    val name= this.getClass.getSimpleName.split("\\$").last
    val tb = new BufferedWriter(new FileWriter("./verilog/"+name+".v"))
    object tbvars {
      val oname=name
      val dutmod = "f2_decimator" 
      val n = 16
      val resolution=32
      val ulimit=resolution-n-1
      val gainbits= 10
      val gainlimit=gainbits-1
      val clk0="cic3clockslow"
      val clk1="hb1clock_low"
      val clk2="hb2clock_low"
      val clk3="hb3clock_low"
      val sig0="cic3integscale"
      val sig1="hb1scale"
      val sig2="hb2scale"
      val sig3="hb3scale"
    }
    //simple template that uses handlebars to input buswidth definition
    val textTemplate="""//This is a tesbench generated with scala generator
                    |//Things you want to control from the simulator cmdline must be parameters
                    |module {{oname}} #( parameter g_infile  = "./A.txt",
                    |                      parameter g_outfile = "./Z.txt",
                    |                      parameter g_Rs_high  = 16*8*20.0e6,
                    |                      parameter g_Rs_low   = 20.0e6,
                    |                      parameter g_scale0   = 1,
                    |                      parameter g_scale1   = 1,
                    |                      parameter g_scale2   = 1,
                    |                      parameter g_scale3   = 1
                    |                      );
                    |//timescale 1ps this should probably be a global model parameter 
                    |parameter integer c_Ts=1/(g_Rs_high*1e-12);
                    |parameter integer c_ratio0=g_Rs_high/(8*g_Rs_low);
                    |parameter integer c_ratio1=g_Rs_high/(4*g_Rs_low);
                    |parameter integer c_ratio2=g_Rs_high/(2*g_Rs_low);
                    |parameter integer c_ratio3=g_Rs_high/(g_Rs_low);
                    |parameter RESET_TIME = 5*c_Ts;
                    |
                    |//These registers always needed
                    |reg clock;
                    |reg reset;
                    |
                    |//Registers for additional clocks
                    |reg io_{{clk0}};
                    |reg io_{{clk1}};
                    |reg io_{{clk2}};
                    |reg io_{{clk3}};
                    |
                    |
                    |//Registers for inputs
                    |reg signed [{{ulimit}}:0] io_iptr_A_real = 0;
                    |reg signed [{{ulimit}}:0] io_iptr_A_imag = 0;
                    |reg signed [{{gainlimit}}:0] io_{{sig0}};
                    |reg signed [{{gainlimit}}:0] io_{{sig1}};
                    |reg signed [{{gainlimit}}:0] io_{{sig2}};
                    |reg signed [{{gainlimit}}:0] io_{{sig3}};
                    |
                    |//Resisters for outputs
                    |wire signed [{{ulimit}}:0] io_Z_real;
                    |wire signed [{{ulimit}}:0] io_Z_imag;
                    |
                    |//File IO parameters
                    |integer StatusI, StatusO, infile, outfile;
                    |integer count0;
                    |integer count1;
                    |integer count2;
                    |integer count3;
                    |integer din1,din2;
                    |
                    |//Initializations
                    |initial count0 = 0;
                    |initial count1 = 0;
                    |initial count2 = 0;
                    |initial count3 = 0;
                    |initial clock = 1'b0;
                    |initial io_{{clk0}}= 1'b0;
                    |initial io_{{clk1}}= 1'b0;
                    |initial io_{{clk2}}= 1'b0;
                    |initial io_{{clk3}}= 1'b0;
                    |initial reset = 1'b0;
                    |//initial io_{{sig1}} =$realtobits($itor(g_scale));
                    |initial outfile = $fopen(g_outfile,"w"); // For writing
                    |
                    |//Clock definitions
                    |always #(c_Ts)clock = !clock ;
                    |always @(posedge clock) begin 
                    |    if (count0%c_ratio0/2 == 0) begin
                    |        io_{{clk0}} =! io_{{clk0}};
                    |    end 
                    |    count0++;
                    |end
                    |always @(posedge clock) begin 
                    |    if (count1%c_ratio1/2 == 0) begin
                    |        io_{{clk1}} =! io_{{clk1}};
                    |    end 
                    |    count1++;
                    |end
                    |always @(posedge clock) begin 
                    |    if (count2%c_ratio2/2 == 0) begin
                    |        io_{{clk2}} =! io_{{clk2}};
                    |    end 
                    |    count2++;
                    |end
                    |always @(posedge clock) begin 
                    |    if (count3%c_ratio3/2 == 0) begin
                    |        io_{{clk3}} =! io_{{clk3}};
                    |    end 
                    |    count3++;
                    |end
                    | 
                    |//always @(posedge io_{{clk0}}) begin 
                    |//always @(posedge io_{{clk1}}) begin 
                    |//always @(posedge io_{{clk2}}) begin 
                    |always @(posedge io_{{clk3}}) begin 
                    |    //Print only valid values 
                    |    if (~($isunknown( io_Z_real)) &&   ~($isunknown( io_Z_imag))) begin
                    |        $fwrite(outfile, "%d\t%d\n", io_Z_real, io_Z_imag);
                    |    end
                    |    else begin
                    |        $fwrite(outfile, "%d\t%d\n", 0, 0);
                    |    end 
                    |end
                    |
                    |//DUT definition
                    |{{dutmod}} DUT ( // @[:@3740.2]
                    |    .clock(clock), // @[:@3741.4]
                    |    .reset(reset), // @[:@3742.4]
                    |    .io_{{clk0}}(io_{{clk0}}), // @[:@3743.4]
                    |    .io_{{clk1}}(io_{{clk1}}), // @[:@3743.4]
                    |    .io_{{clk2}}(io_{{clk2}}), // @[:@3743.4]
                    |    .io_{{clk3}}(io_{{clk3}}), // @[:@3743.4]
                    |    .io_{{sig0}}(io_{{sig0}}), // @[:@3743.4]
                    |    .io_{{sig1}}(io_{{sig1}}), // @[:@3743.4]
                    |    .io_{{sig2}}(io_{{sig2}}), // @[:@3743.4]
                    |    .io_{{sig3}}(io_{{sig3}}), // @[:@3743.4]
                    |    .io_iptr_A_real, // @[:@3743.4]
                    |    .io_iptr_A_imag, // @[:@3743.4]
                    |    .io_Z_real, // @[:@3743.4]
                    |    .io_Z_imag // @[:@3743.4]
                    |   );
                    |
                    |initial #0 begin
                    |    io_{{sig0}} = g_scale0;
                    |    io_{{sig1}} = g_scale1;
                    |    io_{{sig2}} = g_scale2;
                    |    io_{{sig3}} = g_scale3;
                    |    reset=1;
                    |    #RESET_TIME
                    |    reset=0;
                    |    infile = $fopen(g_infile,"r"); // For reading
                    |    while (!$feof(infile)) begin
                    |            @(posedge clock) 
                    |             StatusI=$fscanf(infile, "%d\t%d\n", din1, din2);
                    |             io_iptr_A_real <= din1;
                    |             io_iptr_A_imag <= din2;
                    |    end
                    |    $fclose(infile);
                    |    $fclose(outfile);
                    |    $finish;
                    |end
                    |endmodule""".stripMargin('|')
  val testbench=Handlebars(textTemplate)
  tb write testbench(tbvars)
  tb.close()
  }
}


